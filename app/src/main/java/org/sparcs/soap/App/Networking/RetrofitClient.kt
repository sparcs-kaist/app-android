package org.sparcs.soap.App.Networking

import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import org.sparcs.soap.App.Domain.Helpers.Constants
import org.sparcs.soap.App.Domain.Helpers.TaxiLocationStorage
import org.sparcs.soap.App.Domain.Helpers.TaxiLocationStorageProtocol
import org.sparcs.soap.App.Domain.Helpers.TokenStorage
import org.sparcs.soap.App.Domain.Helpers.TokenStorageProtocol
import org.sparcs.soap.App.Domain.Helpers.UserStorage
import org.sparcs.soap.App.Domain.Helpers.UserStorageProtocol
import org.sparcs.soap.App.Domain.Repositories.Ara.AraBoardRepository
import org.sparcs.soap.App.Domain.Repositories.Ara.AraBoardRepositoryProtocol
import org.sparcs.soap.App.Domain.Repositories.Ara.AraCommentRepository
import org.sparcs.soap.App.Domain.Repositories.Ara.AraCommentRepositoryProtocol
import org.sparcs.soap.App.Domain.Repositories.Ara.AraUserRepository
import org.sparcs.soap.App.Domain.Repositories.Ara.AraUserRepositoryProtocol
import org.sparcs.soap.App.Domain.Repositories.FCMRepository
import org.sparcs.soap.App.Domain.Repositories.FCMRepositoryProtocol
import org.sparcs.soap.App.Domain.Repositories.Feed.FeedCommentRepository
import org.sparcs.soap.App.Domain.Repositories.Feed.FeedCommentRepositoryProtocol
import org.sparcs.soap.App.Domain.Repositories.Feed.FeedImageRepository
import org.sparcs.soap.App.Domain.Repositories.Feed.FeedImageRepositoryProtocol
import org.sparcs.soap.App.Domain.Repositories.Feed.FeedPostRepository
import org.sparcs.soap.App.Domain.Repositories.Feed.FeedPostRepositoryProtocol
import org.sparcs.soap.App.Domain.Repositories.Feed.FeedUserRepository
import org.sparcs.soap.App.Domain.Repositories.Feed.FeedUserRepositoryProtocol
import org.sparcs.soap.App.Domain.Repositories.OTL.OTLCourseRepository
import org.sparcs.soap.App.Domain.Repositories.OTL.OTLCourseRepositoryProtocol
import org.sparcs.soap.App.Domain.Repositories.OTL.OTLLectureRepository
import org.sparcs.soap.App.Domain.Repositories.OTL.OTLLectureRepositoryProtocol
import org.sparcs.soap.App.Domain.Repositories.OTL.OTLTimetableRepository
import org.sparcs.soap.App.Domain.Repositories.OTL.OTLTimetableRepositoryProtocol
import org.sparcs.soap.App.Domain.Repositories.OTL.OTLUserRepository
import org.sparcs.soap.App.Domain.Repositories.OTL.OTLUserRepositoryProtocol
import org.sparcs.soap.App.Domain.Repositories.Taxi.TaxiChatRepository
import org.sparcs.soap.App.Domain.Repositories.Taxi.TaxiChatRepositoryProtocol
import org.sparcs.soap.App.Domain.Repositories.Taxi.TaxiReportRepository
import org.sparcs.soap.App.Domain.Repositories.Taxi.TaxiReportRepositoryProtocol
import org.sparcs.soap.App.Domain.Repositories.Taxi.TaxiRoomRepository
import org.sparcs.soap.App.Domain.Repositories.Taxi.TaxiRoomRepositoryProtocol
import org.sparcs.soap.App.Domain.Repositories.Taxi.TaxiUserRepository
import org.sparcs.soap.App.Domain.Repositories.Taxi.TaxiUserRepositoryProtocol
import org.sparcs.soap.App.Domain.Services.AuthenticationService
import org.sparcs.soap.App.Domain.Services.AuthenticationServiceProtocol
import org.sparcs.soap.App.Domain.Usecases.AuthUseCase
import org.sparcs.soap.App.Domain.Usecases.AuthUseCaseProtocol
import org.sparcs.soap.App.Domain.Usecases.FCMUseCase
import org.sparcs.soap.App.Domain.Usecases.FCMUseCaseProtocol
import org.sparcs.soap.App.Domain.Usecases.TaxiChatUseCase
import org.sparcs.soap.App.Domain.Usecases.TaxiChatUseCaseProtocol
import org.sparcs.soap.App.Domain.Usecases.TaxiLocationUseCase
import org.sparcs.soap.App.Domain.Usecases.TaxiLocationUseCaseProtocol
import org.sparcs.soap.App.Domain.Usecases.TaxiRoomUseCase
import org.sparcs.soap.App.Domain.Usecases.TaxiRoomUseCaseProtocol
import org.sparcs.soap.App.Domain.Usecases.TimetableUseCase
import org.sparcs.soap.App.Domain.Usecases.TimetableUseCaseBackground
import org.sparcs.soap.App.Domain.Usecases.TimetableUseCaseBackgroundProtocol
import org.sparcs.soap.App.Domain.Usecases.TimetableUseCaseProtocol
import org.sparcs.soap.App.Domain.Usecases.UserUseCase
import org.sparcs.soap.App.Domain.Usecases.UserUseCaseProtocol
import org.sparcs.soap.App.Networking.RetrofitAPI.AppVersionApi
import org.sparcs.soap.App.Networking.RetrofitAPI.Ara.AraBoardApi
import org.sparcs.soap.App.Networking.RetrofitAPI.Ara.AraCommentApi
import org.sparcs.soap.App.Networking.RetrofitAPI.Ara.AraUserApi
import org.sparcs.soap.App.Networking.RetrofitAPI.AuthApi
import org.sparcs.soap.App.Networking.RetrofitAPI.FCMApi
import org.sparcs.soap.App.Networking.RetrofitAPI.Feed.FeedCommentApi
import org.sparcs.soap.App.Networking.RetrofitAPI.Feed.FeedImageApi
import org.sparcs.soap.App.Networking.RetrofitAPI.Feed.FeedPostApi
import org.sparcs.soap.App.Networking.RetrofitAPI.Feed.FeedUserApi
import org.sparcs.soap.App.Networking.RetrofitAPI.OTL.OTLCourseApi
import org.sparcs.soap.App.Networking.RetrofitAPI.OTL.OTLLectureApi
import org.sparcs.soap.App.Networking.RetrofitAPI.OTL.OTLTimetableApi
import org.sparcs.soap.App.Networking.RetrofitAPI.OTL.OTLUserApi
import org.sparcs.soap.App.Networking.RetrofitAPI.Taxi.TaxiChatApi
import org.sparcs.soap.App.Networking.RetrofitAPI.Taxi.TaxiReportApi
import org.sparcs.soap.App.Networking.RetrofitAPI.Taxi.TaxiRoomApi
import org.sparcs.soap.App.Networking.RetrofitAPI.Taxi.TaxiUserApi
import org.sparcs.soap.App.Shared.Extensions.AndroidStringProvider
import org.sparcs.soap.App.Shared.Extensions.StringProvider
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton

class TokenAuthenticator @Inject constructor(
    private val authUseCaseProvider: Provider<AuthUseCase>
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) {
            return null
        }
        val authUseCase = authUseCaseProvider.get()
        val newToken = runBlocking { authUseCase.getValidAccessToken() }
        return newToken.let {
            response.request.newBuilder()
                .header("Authorization", "Bearer $it")
                .build()
        }
    }
    private fun responseCount(response: Response): Int {
        var result = 1
        var prior = response.priorResponse
        while (prior != null) {
            result++
            prior = prior.priorResponse
        }
        return result
    }
}

/**
 * NetworkModule
 * StorageModule
 * RepositoryModule
 * ServiceModule
 */

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    @Named("TaxiBackend")
    fun taxiBackEndURL(
        gson: Gson,
        tokenStorage: TokenStorageProtocol,
        tokenAuthenticator: TokenAuthenticator
    ): Retrofit {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val accessToken = runBlocking { tokenStorage.getAccessToken() }
                val newRequest = original.newBuilder()
                    .header("Origin", "sparcsapp")
                    .header("Content-Type", "application/json")
                    .apply { accessToken?.let { header("Authorization", "Bearer $it") } }
                    .build()
                chain.proceed(newRequest)
            }
            .authenticator(tokenAuthenticator)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        return Retrofit.Builder()
            .baseUrl(Constants.taxiBackendURL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    @Named("Auth")
    fun authorizationURL(gson: Gson): Retrofit {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val newRequest = original.newBuilder()
                    .header("Origin", "sparcsapp")
                    .header("Content-Type", "application/json")
                    .build()
                chain.proceed(newRequest)
            }
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        return Retrofit.Builder()
            .baseUrl(Constants.taxiBackendURL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    @Named("AraBackend")
    fun araBackEndURL(
        gson: Gson,
        tokenStorage: TokenStorageProtocol,
        tokenAuthenticator: TokenAuthenticator
    ): Retrofit {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val accessToken = runBlocking { tokenStorage.getAccessToken() }
                val newRequest = original.newBuilder()
                    .header("Origin", "sparcsapp")
                    .header("Content-Type", "application/json")
                    .apply { accessToken?.let { header("Authorization", "Bearer $it") } }
                    .build()
                chain.proceed(newRequest)
            }
            .authenticator(tokenAuthenticator)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        return Retrofit.Builder()
            .baseUrl(Constants.araBackendURL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }


    @Provides
    @Singleton
    @Named("FeedBackend")
    fun feedBackEndURL(
        gson: Gson,
        tokenStorage: TokenStorageProtocol,
        tokenAuthenticator: TokenAuthenticator
    ): Retrofit {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val accessToken = runBlocking { tokenStorage.getAccessToken() }
                val newRequest = original.newBuilder()
                    .header("Origin", "sparcsapp")
                    .header("Content-Type", "application/json")
                    .apply { accessToken?.let { header("Authorization", "Bearer $it") } }
                    .build()
                chain.proceed(newRequest)
            }
            .authenticator(tokenAuthenticator)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        return Retrofit.Builder()
            .baseUrl(Constants.feedBackendURL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    @Named("OTLBackend")
    fun otlBackEndURL(
        gson: Gson,
        tokenStorage: TokenStorageProtocol,
        tokenAuthenticator: TokenAuthenticator
    ): Retrofit {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val accessToken = runBlocking { tokenStorage.getAccessToken() }
                val newRequest = original.newBuilder()
                    .header("Origin", "sparcsapp")
                    .header("Content-Type", "application/json")
                    .apply { accessToken?.let { header("Authorization", "Bearer $it") } }
                    .build()
                chain.proceed(newRequest)
            }
            .authenticator(tokenAuthenticator)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        return Retrofit.Builder()
            .baseUrl(Constants.otlBackendURL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideTaxiRoomApi(@Named("TaxiBackend") retrofit: Retrofit): TaxiRoomApi {
        return retrofit.create(TaxiRoomApi::class.java)
    }

    @Provides
    @Singleton
    @Named("Auth")
    fun provideAuthApi(@Named("Auth") retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideTaxiChatApi(@Named("TaxiBackend") retrofit: Retrofit): TaxiChatApi {
        return retrofit.create(TaxiChatApi::class.java)
    }

    @Provides
    @Singleton
    fun provideTaxiUserApi(@Named("TaxiBackend") retrofit: Retrofit): TaxiUserApi {
        return retrofit.create(TaxiUserApi::class.java)
    }

    @Provides
    @Singleton
    fun provideTaxiReportApi(@Named("TaxiBackend") retrofit: Retrofit): TaxiReportApi {
        return retrofit.create(TaxiReportApi::class.java)
    }
    @Provides
    @Singleton
    fun provideAraBoardApi(@Named("AraBackend") retrofit: Retrofit): AraBoardApi {
        return retrofit.create(AraBoardApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAraCommentApi(@Named("AraBackend") retrofit: Retrofit): AraCommentApi {
        return retrofit.create(AraCommentApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAraUserApi(@Named("AraBackend") retrofit: Retrofit): AraUserApi {
        return retrofit.create(AraUserApi::class.java)
    }

    @Provides
    @Singleton
    fun provideFeedCommentApi(@Named("FeedBackend") retrofit: Retrofit): FeedCommentApi {
        return retrofit.create(FeedCommentApi::class.java)
    }

    @Provides
    @Singleton
    fun provideFeedUserApi(@Named("FeedBackend") retrofit: Retrofit): FeedUserApi {
        return retrofit.create(FeedUserApi::class.java)
    }

    @Provides
    @Singleton
    fun provideFeedImageApi(@Named("FeedBackend") retrofit: Retrofit): FeedImageApi {
        return retrofit.create(FeedImageApi::class.java)
    }

    @Provides
    @Singleton
    fun provideFeedPostApi(@Named("FeedBackend") retrofit: Retrofit): FeedPostApi {
        return retrofit.create(FeedPostApi::class.java)
    }

    @Provides
    @Singleton
    fun provideOTLUserApi(@Named("OTLBackend") retrofit: Retrofit): OTLUserApi {
        return retrofit.create(OTLUserApi::class.java)
    }

    @Provides
    @Singleton
    fun provideOTLTimetableApi(@Named("OTLBackend") retrofit: Retrofit): OTLTimetableApi {
        return retrofit.create(OTLTimetableApi::class.java)
    }

    @Provides
    @Singleton
    fun provideOTLCourseApi(@Named("OTLBackend") retrofit: Retrofit): OTLCourseApi {
        return retrofit.create(OTLCourseApi::class.java)
    }

    @Provides
    @Singleton
    fun provideOTLLectureApi(@Named("OTLBackend") retrofit: Retrofit): OTLLectureApi {
        return retrofit.create(OTLLectureApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAppVersionApi(@Named("FeedBackend") retrofit: Retrofit): AppVersionApi {
        return retrofit.create(AppVersionApi::class.java)
    }

    @Provides
    @Singleton
    fun provideFCMApi(@Named("FeedBackend") retrofit: Retrofit): FCMApi {
        return retrofit.create(FCMApi::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class StorageModule {

    @Binds
    @Singleton
    abstract fun bindTokenStorage(impl: TokenStorage): TokenStorageProtocol

    @Binds
    @Singleton
    abstract fun bindUserStorage(impl: UserStorage): UserStorageProtocol

    @Binds
    @Singleton
    abstract fun bindTaxiLocationStorage(impl: TaxiLocationStorage): TaxiLocationStorageProtocol
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTaxiRoomRepository(
        impl: TaxiRoomRepository
    ): TaxiRoomRepositoryProtocol

    @Binds
    @Singleton
    abstract fun bindTaxiUserRepository(
        impl: TaxiUserRepository
    ): TaxiUserRepositoryProtocol

    @Binds
    @Singleton
    abstract fun bindTaxiReportRepository(
        impl: TaxiReportRepository
    ): TaxiReportRepositoryProtocol

    @Binds
    @Singleton
    abstract fun bindAraBoardRepository(
        impl: AraBoardRepository
    ): AraBoardRepositoryProtocol

    @Binds
    @Singleton
    abstract fun bindAraCommentRepository(
        impl: AraCommentRepository
    ): AraCommentRepositoryProtocol

    @Binds
    @Singleton
    abstract fun bindAraUserRepository(
        impl: AraUserRepository
    ): AraUserRepositoryProtocol

    @Binds
    @Singleton
    abstract fun bindFeedCommentRepository(
        impl: FeedCommentRepository
    ): FeedCommentRepositoryProtocol

    @Binds
    @Singleton
    abstract fun bindFeedUserRepository(
        impl: FeedUserRepository
    ): FeedUserRepositoryProtocol

    @Binds
    @Singleton
    abstract fun bindFeedImageRepository(
        impl: FeedImageRepository
    ): FeedImageRepositoryProtocol

    @Binds
    @Singleton
    abstract fun bindFeedPostRepository(
        impl: FeedPostRepository
    ): FeedPostRepositoryProtocol

    @Binds
    @Singleton
    abstract fun bindOTLUserRepository(
        impl: OTLUserRepository
    ): OTLUserRepositoryProtocol

    @Binds
    @Singleton
    abstract fun bindOTLTimetableRepository(
        impl: OTLTimetableRepository
    ): OTLTimetableRepositoryProtocol

    @Binds
    @Singleton
    abstract fun bindOTLCourseRepository(
        impl: OTLCourseRepository
    ): OTLCourseRepositoryProtocol

    @Binds
    @Singleton
    abstract fun bindOTLLectureRepository(
        impl: OTLLectureRepository
    ): OTLLectureRepositoryProtocol

    @Binds
    @Singleton
    abstract fun bindTaxiChatRepository(
        impl: TaxiChatRepository
    ): TaxiChatRepositoryProtocol

    @Binds
    @Singleton
    abstract fun bindFCMRepository(
        impl: FCMRepository
    ): FCMRepositoryProtocol
}

@Module
@InstallIn(SingletonComponent::class)
abstract class UseCaseModule {

    @Binds
    @Singleton
    abstract fun bindAuthUseCase(impl: AuthUseCase): AuthUseCaseProtocol

    @Binds
    @Singleton
    abstract fun bindUserUseCase(impl: UserUseCase): UserUseCaseProtocol

    @Binds
    @Singleton
    abstract fun bindTaxiChatUseCase(impl: TaxiChatUseCase): TaxiChatUseCaseProtocol

    @Binds
    @Singleton
    abstract fun bindTaxiRoomUseCase(impl: TaxiRoomUseCase): TaxiRoomUseCaseProtocol

    @Binds
    @Singleton
    abstract fun bindTaxiLocationUseCase(impl: TaxiLocationUseCase): TaxiLocationUseCaseProtocol

    @Binds
    @Singleton
    abstract fun bindTimetableUseCase(impl: TimetableUseCase): TimetableUseCaseProtocol

    @Binds
    @Singleton
    abstract fun bindTimetableUseCaseBackground(impl: TimetableUseCaseBackground): TimetableUseCaseBackgroundProtocol

    @Binds
    @Singleton
    abstract fun bindStringProvider(
        impl: AndroidStringProvider
    ): StringProvider

    @Binds
    @Singleton
    abstract fun bindFCMUseCase(
        fcmUseCase: FCMUseCase
    ): FCMUseCaseProtocol
}

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    @Provides
    @Singleton
    fun provideAuthenticationService(
        @Named("Auth") authApi: AuthApi,
        tokenStorage: TokenStorageProtocol
    ): AuthenticationServiceProtocol {
        return AuthenticationService(authApi, tokenStorage)
    }
}