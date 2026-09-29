package org.sparcs.soap.friendsTests

import com.google.gson.Gson
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.sparcs.soap.app.domain.error.NetworkError
import org.sparcs.soap.app.domain.error.otl.FriendUseCaseError
import org.sparcs.soap.app.domain.helpers.FriendCode
import org.sparcs.soap.app.domain.models.otl.Friend
import org.sparcs.soap.app.domain.repositories.otl.OTLFriendRepository
import org.sparcs.soap.app.domain.repositories.otl.OTLFriendRepositoryProtocol
import org.sparcs.soap.app.domain.models.otl.FriendList
import org.sparcs.soap.app.domain.usecases.otl.FriendUseCase
import org.sparcs.soap.app.networking.NetworkModule
import org.sparcs.soap.app.networking.responseDTO.otl.FriendListResponseDTO
import org.sparcs.soap.buddyTestSupport.MockCrashlyticsService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.Instant

class FriendDataTest {
    private val gson = Gson()

    @Test fun `only six ASCII letters and digits are accepted`() {
        assertEquals("ACD347", FriendCode.normalized(" acD347\n"))
        listOf("", "ACD34", "ACD3477", "ACD!47", "ＡＣＤ３４７", "acdß47", "AC D47").forEach {
            assertNull(FriendCode.normalized(it))
        }
    }

    @Test fun `sanitizing drops everything a code can't contain`() {
        assertEquals("ACD347", FriendCode.sanitized("a-c d3４47xyz"))
        assertEquals("", FriendCode.sanitized("가나다!!"))
    }

    @Test fun `friend list parses checkedAt with and without fractional seconds`() {
        val withFraction = """{"checkedAt":"2026-09-28T04:31:18.000Z","friends":[{"id":1,"name":"Sujin","isFavorite":true,"hasScheduleNow":false}]}"""
        val plain = """{"checkedAt":"2026-09-28T04:31:18Z","friends":[]}"""
        val expected = Instant.parse("2026-09-28T04:31:18Z")

        val list = gson.fromJson(withFraction, FriendListResponseDTO::class.java).toModel()
        assertEquals(expected, list.checkedAt)
        assertEquals(listOf(Friend(1, "Sujin", isFavorite = true, hasScheduleNow = false)), list.friends)
        assertEquals(expected, gson.fromJson(plain, FriendListResponseDTO::class.java).toModel().checkedAt)
    }

    @Test fun `missing or malformed checkedAt and friends degrade gracefully`() {
        val list = gson.fromJson("""{"checkedAt":"yesterday"}""", FriendListResponseDTO::class.java).toModel()
        assertNull(list.checkedAt)
        assertTrue(list.friends.isEmpty())
    }

    @Test fun `repository uses the iOS endpoints and request bodies`() = runTest {
        val requests = mutableListOf<Request>()
        val repository = repository(200) { requests.add(it) }

        repository.fetchFriends()
        repository.addFriend("ACD347")
        repository.deleteFriend(7)
        repository.setFavorite(7, isFavorite = true)
        repository.fetchMyCode()

        assertEquals(
            listOf(
                "GET /api/v2/friends",
                "POST /api/v2/friends",
                "DELETE /api/v2/friends/7",
                "PATCH /api/v2/friends/7/favorite",
                "GET /api/v2/friends/code"
            ),
            requests.map { "${it.method} ${it.url.encodedPath}" }
        )
        assertEquals(mapOf("code" to "ACD347"), requests[1].jsonBody())
        assertEquals(mapOf("isFavorite" to true), requests[3].jsonBody())
    }

    @Test fun `repository reads my code`() = runTest {
        assertEquals("ACD347", repository(200).fetchMyCode())
    }

    @Test fun `repository propagates server failures`() = runTest {
        for (status in listOf(400, 404, 500)) {
            var actualStatus: Int? = null
            try {
                repository(status).addFriend("ACD347")
            } catch (error: NetworkError.ServerError) {
                actualStatus = error.code
            }
            assertEquals(status, actualStatus)
        }
    }

    @Test fun `use case records failures and wraps unknown errors`() = runTest {
        val crashlytics = MockCrashlyticsService()
        val useCase = FriendUseCase(FailingRepository(IllegalStateException("boom")), crashlytics)

        var thrown: Throwable? = null
        try {
            useCase.deleteFriend(3)
        } catch (error: Throwable) {
            thrown = error
        }

        assertTrue(thrown is FriendUseCaseError.Unknown)
        assertEquals("Friend", crashlytics.lastRecordedContext?.feature)
        assertEquals("deleteFriend", crashlytics.lastRecordedContext?.action)
        assertEquals("3", crashlytics.lastRecordedContext?.metadata?.get("friendID"))
    }

    @Test fun `use case passes network errors through unchanged`() = runTest {
        val useCase = FriendUseCase(FailingRepository(NetworkError.NotFound()), MockCrashlyticsService())
        var thrown: Throwable? = null
        try {
            useCase.fetchMyCode()
        } catch (error: Throwable) {
            thrown = error
        }
        assertTrue(thrown is NetworkError.NotFound)
    }

    private fun Request.jsonBody(): Map<*, *> {
        val buffer = Buffer()
        body!!.writeTo(buffer)
        return gson.fromJson(buffer.readUtf8(), Map::class.java)
    }

    private fun repository(status: Int, onRequest: (Request) -> Unit = {}): OTLFriendRepository {
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            onRequest(chain.request())
            val path = chain.request().url.encodedPath
            val body = when {
                path.endsWith("/code") -> """{"code":"ACD347"}"""
                chain.request().method == "GET" -> """{"checkedAt":null,"friends":[]}"""
                else -> ""
            }
            Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(status)
                .message("Test response")
                .body(body.toResponseBody("application/json".toMediaType()))
                .build()
        }.build()
        val retrofit = Retrofit.Builder().baseUrl("https://example.test/")
            .client(client).addConverterFactory(GsonConverterFactory.create(gson)).build()
        return OTLFriendRepository(NetworkModule.provideOTLFriendApi(retrofit), gson)
    }

    private class FailingRepository(private val error: Exception) : OTLFriendRepositoryProtocol {
        override suspend fun fetchFriends(): FriendList = throw error
        override suspend fun addFriend(code: String) = throw error
        override suspend fun deleteFriend(id: Int) = throw error
        override suspend fun setFavorite(id: Int, isFavorite: Boolean) = throw error
        override suspend fun fetchMyCode(): String = throw error
    }
}
