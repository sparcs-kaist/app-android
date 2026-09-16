package org.sparcs.soap.timetableTests

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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.sparcs.soap.app.domain.helpers.TimetableTheme
import org.sparcs.soap.app.domain.helpers.TimetableThemeShareCode
import org.sparcs.soap.app.domain.error.NetworkError
import org.sparcs.soap.app.domain.repositories.settings.TimetableThemeRepository
import org.sparcs.soap.app.networking.NetworkModule
import org.sparcs.soap.app.networking.responseDTO.TimetableThemeDTO
import org.sparcs.soap.app.networking.responseDTO.TimetableThemeShareResponseDTO
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TimetableThemeSharingTest {
    private val gson = Gson()

    @Test fun `only six ASCII letters and digits are accepted`() {
        assertEquals("ABC123", TimetableThemeShareCode.normalized(" abC123\n"))
        listOf("", "ABC12", "ABC1234", "ABC!23", "ＡＢＣ１２３", "abcß12", "AB C12").forEach {
            assertNull(TimetableThemeShareCode.normalized(it))
        }
    }

    @Test fun `iOS response imports independent editable themes with every color preserved`() {
        val json = """{"code":"ABC123","theme":{"name":"Ocean","hexColors":["123456","ABCDEF"],"textColorHex":"FFFFFF","separatorColorHex":"111111","backgroundColorHex":"222222","gridLabelColorHex":"333333"}}"""
        val response = gson.fromJson(json, TimetableThemeShareResponseDTO::class.java)
        val first = response.theme.toModel()
        val second = response.theme.toModel()
        assertNotEquals(first.id, second.id)
        assertTrue(first.id.startsWith("custom."))
        assertFalse(first.isBuiltIn)
        assertEquals("Ocean", first.name)
        assertEquals(listOf("123456", "ABCDEF"), first.hexColors)
        assertEquals("FFFFFF", first.textColorHex)
        assertEquals("111111", first.separatorColorHex)
        assertEquals("222222", first.backgroundColorHex)
        assertEquals("333333", first.gridLabelColorHex)
    }

    @Test fun `sharing excludes local identity and preserves payload`() {
        val theme = TimetableTheme.Default.copy(name = "기본")
        val payload = gson.toJsonTree(TimetableThemeDTO.fromModel(theme)).asJsonObject
        assertFalse(payload.has("id"))
        assertFalse(payload.has("isBuiltIn"))
        assertEquals("기본", payload["name"].asString)
        assertEquals(theme.hexColors.size, payload["hexColors"].asJsonArray.size())
    }

    @Test fun `invalid or incomplete remote themes cannot enter local storage`() {
        listOf(
            """{"name":"Broken","hexColors":["123456"]}""",
            """{"name":"Broken","hexColors":[],"textColorHex":"FFFFFF"}""",
            """{"name":"Broken","hexColors":["ZZZZZZ"],"textColorHex":"FFFFFF"}""",
            """{"name":" ","hexColors":["123456"],"textColorHex":"FFFFFF"}"""
        ).forEach { json ->
            assertThrows(IllegalArgumentException::class.java) {
                gson.fromJson(json, TimetableThemeDTO::class.java).toModel()
            }
        }
    }

    @Test fun `repository uses the iOS endpoints and a theme-only request body`() = runTest {
        val requests = mutableListOf<Request>()
        val repository = repository(200) { requests.add(it) }
        assertEquals("ABC123", repository.share(TimetableTheme.Default))
        assertEquals("Ocean", repository.fetch("ABC123").name)
        assertEquals("POST", requests[0].method)
        assertEquals("/v1/timetable-themes/shares", requests[0].url.encodedPath)
        assertEquals("GET", requests[1].method)
        assertEquals("/v1/timetable-themes/shares/ABC123", requests[1].url.encodedPath)
        val buffer = Buffer()
        requests[0].body!!.writeTo(buffer)
        val body = gson.fromJson(buffer.readUtf8(), Map::class.java)
        assertFalse(body.containsKey("id"))
        assertFalse(body.containsKey("isBuiltIn"))
        assertEquals(TimetableTheme.Default.hexColors, body["hexColors"])
    }

    @Test fun `repository propagates missing codes throttling and server failures`() = runTest {
        for (status in listOf(404, 429, 500)) {
            var actualStatus: Int? = null
            try { repository(status).fetch("ABC123") }
            catch (error: NetworkError.ServerError) { actualStatus = error.code }
            assertEquals(status, actualStatus)
        }
    }

    private fun repository(status: Int, onRequest: (Request) -> Unit = {}): TimetableThemeRepository {
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            onRequest(chain.request())
            Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(status)
                .message("Test response")
                .body("""{"code":"ABC123","theme":{"name":"Ocean","hexColors":["123456"],"textColorHex":"FFFFFF"}}""".toResponseBody("application/json".toMediaType()))
                .build()
        }.build()
        val retrofit = Retrofit.Builder().baseUrl("https://example.test/v1/")
            .client(client).addConverterFactory(GsonConverterFactory.create(gson)).build()
        return TimetableThemeRepository(NetworkModule.provideTimetableThemeApi(retrofit), gson)
    }
}
