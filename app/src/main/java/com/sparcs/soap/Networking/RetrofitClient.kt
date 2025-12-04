package com.sparcs.soap.Networking

import android.util.Log
import com.google.gson.Gson
import com.sparcs.soap.BuildConfig
import com.sparcs.soap.Domain.Helpers.Constants
import com.sparcs.soap.Domain.Helpers.TaxiLocationStorage
import com.sparcs.soap.Domain.Helpers.TaxiLocationStorageProtocol
import com.sparcs.soap.Domain.Helpers.TokenStorage
import com.sparcs.soap.Domain.Helpers.TokenStorageProtocol
import com.sparcs.soap.Domain.Helpers.UserStorage
import com.sparcs.soap.Domain.Helpers.UserStorageProtocol
import com.sparcs.soap.Domain.Repositories.Ara.AraBoardRepository
import com.sparcs.soap.Domain.Repositories.Ara.AraBoardRepositoryProtocol
import com.sparcs.soap.Domain.Repositories.Ara.AraCommentRepository
import com.sparcs.soap.Domain.Repositories.Ara.AraCommentRepositoryProtocol
import com.sparcs.soap.Domain.Repositories.Ara.AraUserRepository
import com.sparcs.soap.Domain.Repositories.Ara.AraUserRepositoryProtocol
import com.sparcs.soap.Domain.Repositories.Feed.FeedCommentRepository
import com.sparcs.soap.Domain.Repositories.Feed.FeedCommentRepositoryProtocol
import com.sparcs.soap.Domain.Repositories.Feed.FeedImageRepository
import com.sparcs.soap.Domain.Repositories.Feed.FeedImageRepositoryProtocol
import com.sparcs.soap.Domain.Repositories.Feed.FeedPostRepository
import com.sparcs.soap.Domain.Repositories.Feed.FeedPostRepositoryProtocol
import com.sparcs.soap.Domain.Repositories.Feed.FeedUserRepository
import com.sparcs.soap.Domain.Repositories.Feed.FeedUserRepositoryProtocol
import com.sparcs.soap.Domain.Repositories.OTL.OTLCourseRepository
import com.sparcs.soap.Domain.Repositories.OTL.OTLCourseRepositoryProtocol
import com.sparcs.soap.Domain.Repositories.OTL.OTLLectureRepository
import com.sparcs.soap.Domain.Repositories.OTL.OTLLectureRepositoryProtocol
import com.sparcs.soap.Domain.Repositories.OTL.OTLTimetableRepository
import com.sparcs.soap.Domain.Repositories.OTL.OTLTimetableRepositoryProtocol
import com.sparcs.soap.Domain.Repositories.OTL.OTLUserRepository
import com.sparcs.soap.Domain.Repositories.OTL.OTLUserRepositoryProtocol
import com.sparcs.soap.Domain.Repositories.Taxi.TaxiChatRepository
import com.sparcs.soap.Domain.Repositories.Taxi.TaxiChatRepositoryProtocol
import com.sparcs.soap.Domain.Repositories.Taxi.TaxiReportRepository
import com.sparcs.soap.Domain.Repositories.Taxi.TaxiReportRepositoryProtocol
import com.sparcs.soap.Domain.Repositories.Taxi.TaxiRoomRepository
import com.sparcs.soap.Domain.Repositories.Taxi.TaxiRoomRepositoryProtocol
import com.sparcs.soap.Domain.Repositories.Taxi.TaxiUserRepository
import com.sparcs.soap.Domain.Repositories.Taxi.TaxiUserRepositoryProtocol
import com.sparcs.soap.Domain.Services.AuthenticationService
import com.sparcs.soap.Domain.Services.AuthenticationServiceProtocol
import com.sparcs.soap.Domain.Usecases.AuthUseCase
import com.sparcs.soap.Domain.Usecases.AuthUseCaseProtocol
import com.sparcs.soap.Domain.Usecases.TaxiChatUseCase
import com.sparcs.soap.Domain.Usecases.TaxiChatUseCaseProtocol
import com.sparcs.soap.Domain.Usecases.TaxiLocationUseCase
import com.sparcs.soap.Domain.Usecases.TaxiLocationUseCaseProtocol
import com.sparcs.soap.Domain.Usecases.TaxiRoomUseCase
import com.sparcs.soap.Domain.Usecases.TaxiRoomUseCaseProtocol
import com.sparcs.soap.Domain.Usecases.TimetableUseCase
import com.sparcs.soap.Domain.Usecases.TimetableUseCaseProtocol
import com.sparcs.soap.Domain.Usecases.UserUseCase
import com.sparcs.soap.Domain.Usecases.UserUseCaseProtocol
import com.sparcs.soap.Networking.RetrofitAPI.Ara.AraBoardApi
import com.sparcs.soap.Networking.RetrofitAPI.Ara.AraCommentApi
import com.sparcs.soap.Networking.RetrofitAPI.Ara.AraUserApi
import com.sparcs.soap.Networking.RetrofitAPI.AuthApi
import com.sparcs.soap.Networking.RetrofitAPI.Feed.FeedCommentApi
import com.sparcs.soap.Networking.RetrofitAPI.Feed.FeedImageApi
import com.sparcs.soap.Networking.RetrofitAPI.Feed.FeedPostApi
import com.sparcs.soap.Networking.RetrofitAPI.Feed.FeedUserApi
import com.sparcs.soap.Networking.RetrofitAPI.OTL.OTLCourseApi
import com.sparcs.soap.Networking.RetrofitAPI.OTL.OTLLectureApi
import com.sparcs.soap.Networking.RetrofitAPI.OTL.OTLTimetableApi
import com.sparcs.soap.Networking.RetrofitAPI.OTL.OTLUserApi
import com.sparcs.soap.Networking.RetrofitAPI.Taxi.TaxiChatApi
import com.sparcs.soap.Networking.RetrofitAPI.Taxi.TaxiReportApi
import com.sparcs.soap.Networking.RetrofitAPI.Taxi.TaxiRoomApi
import com.sparcs.soap.Networking.RetrofitAPI.Taxi.TaxiUserApi
import com.sparcs.soap.Shared.Extensions.AndroidStringProvider
import com.sparcs.soap.Shared.Extensions.StringProvider
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
        Log.d("AUTH", "we need provider right?")
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
        tokenStorage: TokenStorageProtocol
    ): Retrofit {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val accessToken = runBlocking { tokenStorage.getAccessToken() }
                val newRequest = original.newBuilder()
                    .header("Origin", "sparcsapp")
                    .header("Content-Type", "application/json")
                    .apply {
                        accessToken?.let {
                            header("Authorization", "Bearer $it")
                        }
                    }
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
                val accessToken = runBlocking { tokenStorage.getAccessToken() } // 단순 토큰
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
        tokenStorage: TokenStorageProtocol
    ): Retrofit {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val accessToken = runBlocking { tokenStorage.getAccessToken() }
                val newRequest = original.newBuilder()
                    .header("Origin", "sparcsapp")
                    .header("Content-Type", "application/json")
                    .apply {
                        accessToken?.let {
                            header("Authorization", "Bearer $it")
                        }
                    }
                    .build()
                chain.proceed(newRequest)
            }
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
        tokenStorage: TokenStorageProtocol
    ): Retrofit {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val accessToken = runBlocking { tokenStorage.getAccessToken() }
                val newRequest = original.newBuilder()
                    .apply {
                        header("Origin", "sparcsapp")
                        header("Content-Type", "application/json")
                        if (BuildConfig.DEBUG) {
                            addHeader("X-SID-AUTH-TOKEN", BuildConfig.OTL_SID_AUTH_TOKEN)
                        }
                        accessToken?.let {
                            header("Authorization", "Bearer $it")
                        }
                    }
                    .build()
                chain.proceed(newRequest)
            }
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
    abstract fun bindStringProvider(
        impl: AndroidStringProvider
    ): StringProvider
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