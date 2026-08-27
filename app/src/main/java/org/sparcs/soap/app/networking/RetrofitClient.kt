package org.sparcs.soap.app.networking

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import org.sparcs.soap.BuildConfig
import org.sparcs.soap.app.cache.AppDatabase
import org.sparcs.soap.app.cache.TaxiRouteCacheDAO
import org.sparcs.soap.app.cache.TimetableCacheDAO
import org.sparcs.soap.app.domain.helpers.Constants
import org.sparcs.soap.app.domain.helpers.TaxiLocationStorage
import org.sparcs.soap.app.domain.helpers.TaxiLocationStorageProtocol
import org.sparcs.soap.app.domain.helpers.TokenStorage
import org.sparcs.soap.app.domain.helpers.TokenStorageProtocol
import org.sparcs.soap.app.domain.helpers.UserStorage
import org.sparcs.soap.app.domain.helpers.UserStorageProtocol
import org.sparcs.soap.app.domain.repositories.AuthRepository
import org.sparcs.soap.app.domain.repositories.AuthRepositoryProtocol
import org.sparcs.soap.app.domain.repositories.FCMRepository
import org.sparcs.soap.app.domain.repositories.FCMRepositoryProtocol
import org.sparcs.soap.app.domain.repositories.ara.AraBoardRepository
import org.sparcs.soap.app.domain.repositories.ara.AraBoardRepositoryProtocol
import org.sparcs.soap.app.domain.repositories.ara.AraCommentRepository
import org.sparcs.soap.app.domain.repositories.ara.AraCommentRepositoryProtocol
import org.sparcs.soap.app.domain.repositories.ara.AraUserRepository
import org.sparcs.soap.app.domain.repositories.ara.AraUserRepositoryProtocol
import org.sparcs.soap.app.domain.repositories.feed.FeedCommentRepository
import org.sparcs.soap.app.domain.repositories.feed.FeedCommentRepositoryProtocol
import org.sparcs.soap.app.domain.repositories.feed.FeedImageRepository
import org.sparcs.soap.app.domain.repositories.feed.FeedImageRepositoryProtocol
import org.sparcs.soap.app.domain.repositories.feed.FeedPostRepository
import org.sparcs.soap.app.domain.repositories.feed.FeedPostRepositoryProtocol
import org.sparcs.soap.app.domain.repositories.feed.FeedProfileRepository
import org.sparcs.soap.app.domain.repositories.feed.FeedProfileRepositoryProtocol
import org.sparcs.soap.app.domain.repositories.feed.FeedUserRepository
import org.sparcs.soap.app.domain.repositories.feed.FeedUserRepositoryProtocol
import org.sparcs.soap.app.domain.repositories.otl.OTLCourseRepository
import org.sparcs.soap.app.domain.repositories.otl.OTLCourseRepositoryProtocol
import org.sparcs.soap.app.domain.repositories.otl.OTLLectureRepository
import org.sparcs.soap.app.domain.repositories.otl.OTLLectureRepositoryProtocol
import org.sparcs.soap.app.domain.repositories.otl.OTLReviewRepository
import org.sparcs.soap.app.domain.repositories.otl.OTLReviewRepositoryProtocol
import org.sparcs.soap.app.domain.repositories.otl.OTLTimetableRepository
import org.sparcs.soap.app.domain.repositories.otl.OTLTimetableRepositoryProtocol
import org.sparcs.soap.app.domain.repositories.otl.OTLUserRepository
import org.sparcs.soap.app.domain.repositories.otl.OTLUserRepositoryProtocol
import org.sparcs.soap.app.domain.repositories.taxi.TaxiChatRepository
import org.sparcs.soap.app.domain.repositories.taxi.TaxiChatRepositoryProtocol
import org.sparcs.soap.app.domain.repositories.taxi.TaxiReportRepository
import org.sparcs.soap.app.domain.repositories.taxi.TaxiReportRepositoryProtocol
import org.sparcs.soap.app.domain.repositories.taxi.TaxiRoomRepository
import org.sparcs.soap.app.domain.repositories.taxi.TaxiRoomRepositoryProtocol
import org.sparcs.soap.app.domain.repositories.taxi.TaxiUserRepository
import org.sparcs.soap.app.domain.repositories.taxi.TaxiUserRepositoryProtocol
import org.sparcs.soap.app.domain.services.AnalyticsService
import org.sparcs.soap.app.domain.services.AnalyticsServiceProtocol
import org.sparcs.soap.app.domain.services.AuthenticationService
import org.sparcs.soap.app.domain.services.AuthenticationServiceProtocol
import org.sparcs.soap.app.domain.services.CrashlyticsService
import org.sparcs.soap.app.domain.services.CrashlyticsServiceProtocol
import org.sparcs.soap.app.domain.services.TaxiChatService
import org.sparcs.soap.app.domain.usecases.AuthUseCase
import org.sparcs.soap.app.domain.usecases.AuthUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.FCMUseCase
import org.sparcs.soap.app.domain.usecases.FCMUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.UserUseCase
import org.sparcs.soap.app.domain.usecases.UserUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.ara.AraBoardUseCase
import org.sparcs.soap.app.domain.usecases.ara.AraBoardUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.ara.AraCommentUseCase
import org.sparcs.soap.app.domain.usecases.ara.AraCommentUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.feed.FeedCommentUseCase
import org.sparcs.soap.app.domain.usecases.feed.FeedCommentUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.feed.FeedImageUseCase
import org.sparcs.soap.app.domain.usecases.feed.FeedImageUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.feed.FeedPostUseCase
import org.sparcs.soap.app.domain.usecases.feed.FeedPostUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.feed.FeedProfileUseCase
import org.sparcs.soap.app.domain.usecases.feed.FeedProfileUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.otl.CourseUseCase
import org.sparcs.soap.app.domain.usecases.otl.CourseUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.otl.LectureUseCase
import org.sparcs.soap.app.domain.usecases.otl.LectureUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.otl.ReviewUseCase
import org.sparcs.soap.app.domain.usecases.otl.ReviewUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.otl.TimetableUseCase
import org.sparcs.soap.app.domain.usecases.otl.TimetableUseCaseBackground
import org.sparcs.soap.app.domain.usecases.otl.TimetableUseCaseBackgroundProtocol
import org.sparcs.soap.app.domain.usecases.otl.TimetableUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.summarization.SummarizationUseCase
import org.sparcs.soap.app.domain.usecases.summarization.SummarizationUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.taxi.TaxiChatUseCase
import org.sparcs.soap.app.domain.usecases.taxi.TaxiChatUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.taxi.TaxiFavoriteUseCase
import org.sparcs.soap.app.domain.usecases.taxi.TaxiFavoriteUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.taxi.TaxiLocationUseCase
import org.sparcs.soap.app.domain.usecases.taxi.TaxiLocationUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.taxi.TaxiRoomUseCase
import org.sparcs.soap.app.domain.usecases.taxi.TaxiRoomUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.translation.PostTranslationUseCase
import org.sparcs.soap.app.domain.usecases.translation.PostTranslationUseCaseProtocol
import org.sparcs.soap.app.networking.responseDTO.AuthRetryConfig
import org.sparcs.soap.app.networking.retrofitAPI.AppVersionApi
import org.sparcs.soap.app.networking.retrofitAPI.AuthApi
import org.sparcs.soap.app.networking.retrofitAPI.FCMApi
import org.sparcs.soap.app.networking.retrofitAPI.ara.AraBoardApi
import org.sparcs.soap.app.networking.retrofitAPI.ara.AraCommentApi
import org.sparcs.soap.app.networking.retrofitAPI.ara.AraUserApi
import org.sparcs.soap.app.networking.retrofitAPI.feed.FeedCommentApi
import org.sparcs.soap.app.networking.retrofitAPI.feed.FeedImageApi
import org.sparcs.soap.app.networking.retrofitAPI.feed.FeedPostApi
import org.sparcs.soap.app.networking.retrofitAPI.feed.FeedProfileApi
import org.sparcs.soap.app.networking.retrofitAPI.feed.FeedUserApi
import org.sparcs.soap.app.networking.retrofitAPI.otl.OTLCourseApi
import org.sparcs.soap.app.networking.retrofitAPI.otl.OTLLectureApi
import org.sparcs.soap.app.networking.retrofitAPI.otl.OTLReviewApi
import org.sparcs.soap.app.networking.retrofitAPI.otl.OTLTimetableApi
import org.sparcs.soap.app.networking.retrofitAPI.otl.OTLUserApi
import org.sparcs.soap.app.networking.retrofitAPI.taxi.TaxiChatApi
import org.sparcs.soap.app.networking.retrofitAPI.taxi.TaxiReportApi
import org.sparcs.soap.app.networking.retrofitAPI.taxi.TaxiRoomApi
import org.sparcs.soap.app.networking.retrofitAPI.taxi.TaxiUserApi
import org.sparcs.soap.app.shared.extensions.AndroidStringProvider
import org.sparcs.soap.app.shared.extensions.StringProvider
import org.sparcs.soap.app.shared.viewModels.TextProcessingDelegate
import org.sparcs.soap.app.shared.viewModels.TextProcessingProtocol
import org.sparcs.soap.widgets.WidgetSyncHelper
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton

class TokenAuthenticator @Inject constructor(
    private val authUseCaseProvider: Provider<AuthUseCaseProtocol>,
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) return null

        val authUseCase = authUseCaseProvider.get()

        return try {
            val newToken = runBlocking(Dispatchers.IO) {
                authUseCase.getValidAccessToken()
            }

            if (newToken.isBlank()) return null

            response.request.newBuilder()
                .header("Authorization", "Bearer $newToken")
                .build()
        } catch (e: Exception) {
            Timber.e(e, "Authentication aborted: Token refresh failed")
            null
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
        tokenAuthenticator: TokenAuthenticator,
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
            .baseUrl(Constants.TAXI_BACKEND_URL)
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
            .baseUrl(Constants.TAXI_BACKEND_URL)
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
        tokenAuthenticator: TokenAuthenticator,
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
            .baseUrl(Constants.ARA_BACKEND_URL)
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
        tokenAuthenticator: TokenAuthenticator,
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
            .baseUrl(Constants.FEED_BACKEND_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    @Named("OTLBackend")
    fun otlBackEndURL(
        @ApplicationContext context: Context,
        gson: Gson,
        tokenStorage: TokenStorageProtocol,
        tokenAuthenticator: TokenAuthenticator,
    ): Retrofit {
        val okHttpClientBuilder = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val accessToken = runBlocking { tokenStorage.getAccessToken() }
                val languageTag = context.resources.configuration.locales[0].language
                val newRequest = original.newBuilder()
                    .header("Origin", "sparcsapp")
                    .header("Accept-Language", languageTag)
                    .header("Content-Type", "application/json")
                    .apply {
                        if (BuildConfig.DEBUG) {
                            addHeader("X-SID-AUTH-TOKEN", BuildConfig.OTL_SID_AUTH_TOKEN)
                        }
                        accessToken?.let { header("Authorization", "Bearer $it") }
                    }
                    .build()
                chain.proceed(newRequest)
            }
            .authenticator(tokenAuthenticator)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })

        return Retrofit.Builder()
            .baseUrl(Constants.OTL_BACKEND_URL)
            .client(okHttpClientBuilder.build())
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
    fun provideOTLReviewApi(@Named("OTLBackend") retrofit: Retrofit): OTLReviewApi {
        return retrofit.create(OTLReviewApi::class.java)
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

    @Provides
    @Singleton
    fun provideFeedProfileApi(@Named("FeedBackend") retrofit: Retrofit): FeedProfileApi {
        return retrofit.create(FeedProfileApi::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
@Suppress("unused")
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
@Suppress("unused")
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTaxiRoomRepository(
        impl: TaxiRoomRepository,
    ): TaxiRoomRepositoryProtocol

    @Binds
    @Singleton
    abstract fun bindTaxiUserRepository(
        impl: TaxiUserRepository,
    ): TaxiUserRepositoryProtocol

    @Binds
    @Singleton
    abstract fun bindTaxiReportRepository(
        impl: TaxiReportRepository,
    ): TaxiReportRepositoryProtocol

    @Binds
    @Singleton
    abstract fun bindAraBoardRepository(
        impl: AraBoardRepository,
    ): AraBoardRepositoryProtocol

    @Binds
    @Singleton
    abstract fun bindAraCommentRepository(
        impl: AraCommentRepository,
    ): AraCommentRepositoryProtocol

    @Binds
    @Singleton
    abstract fun bindAraUserRepository(
        impl: AraUserRepository,
    ): AraUserRepositoryProtocol

    @Binds
    @Singleton
    abstract fun bindFeedCommentRepository(
        impl: FeedCommentRepository,
    ): FeedCommentRepositoryProtocol

    @Binds
    @Singleton
    abstract fun bindFeedUserRepository(
        impl: FeedUserRepository,
    ): FeedUserRepositoryProtocol


    @Binds
    @Singleton
    abstract fun bindFeedProfileRepository(
        impl: FeedProfileRepository,
    ): FeedProfileRepositoryProtocol

    @Binds
    @Singleton
    abstract fun bindFeedImageRepository(
        impl: FeedImageRepository,
    ): FeedImageRepositoryProtocol

    @Binds
    @Singleton
    abstract fun bindFeedPostRepository(
        impl: FeedPostRepository,
    ): FeedPostRepositoryProtocol

    @Binds
    @Singleton
    abstract fun bindOTLUserRepository(
        impl: OTLUserRepository,
    ): OTLUserRepositoryProtocol

    @Binds
    @Singleton
    abstract fun bindOTLTimetableRepository(
        impl: OTLTimetableRepository,
    ): OTLTimetableRepositoryProtocol

    @Binds
    @Singleton
    abstract fun bindOTLCourseRepository(
        impl: OTLCourseRepository,
    ): OTLCourseRepositoryProtocol

    @Binds
    @Singleton
    abstract fun bindOTLLectureRepository(
        impl: OTLLectureRepository,
    ): OTLLectureRepositoryProtocol

    @Binds
    @Singleton
    abstract fun bindOTLReviewRepository(
        impl: OTLReviewRepository,
    ): OTLReviewRepositoryProtocol

    @Binds
    @Singleton
    abstract fun bindTaxiChatRepository(
        impl: TaxiChatRepository,
    ): TaxiChatRepositoryProtocol

    @Binds
    @Singleton
    abstract fun bindFCMRepository(
        impl: FCMRepository,
    ): FCMRepositoryProtocol

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepository,
    ): AuthRepositoryProtocol
}

@Module
@InstallIn(SingletonComponent::class)
@Suppress("unused")
abstract class UseCaseModule {

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
    abstract fun bindTaxiFavoriteUseCase(impl: TaxiFavoriteUseCase): TaxiFavoriteUseCaseProtocol

    @Binds
    @Singleton
    abstract fun bindTimetableUseCase(impl: TimetableUseCase): TimetableUseCaseProtocol

    @Binds
    @Singleton
    abstract fun bindTimetableUseCaseBackground(impl: TimetableUseCaseBackground): TimetableUseCaseBackgroundProtocol

    @Binds
    @Singleton
    abstract fun bindStringProvider(
        impl: AndroidStringProvider,
    ): StringProvider

    @Binds
    @Singleton
    abstract fun bindFCMUseCase(
        fcmUseCase: FCMUseCase,
    ): FCMUseCaseProtocol

    @Binds
    @Singleton
    abstract fun bindFeedCommentUseCase(
        impl: FeedCommentUseCase,
    ): FeedCommentUseCaseProtocol

    @Binds
    @Singleton
    abstract fun bindFeedPostUseCase(
        impl: FeedPostUseCase,
    ): FeedPostUseCaseProtocol


    @Binds
    @Singleton
    abstract fun bindCrashlyticsService(
        impl: CrashlyticsService,
    ): CrashlyticsServiceProtocol

    @Binds
    @Singleton
    abstract fun bindAnalyticsService(
        impl: AnalyticsService,
    ): AnalyticsServiceProtocol

    @Binds
    @Singleton
    abstract fun bindAraBoardUseCase(
        impl: AraBoardUseCase,
    ): AraBoardUseCaseProtocol


    @Binds
    @Singleton
    abstract fun bindFeedImageUseCase(
        impl: FeedImageUseCase,
    ): FeedImageUseCaseProtocol

    @Binds
    @Singleton
    abstract fun bindAraCommentUseCase(
        impl: AraCommentUseCase,
    ): AraCommentUseCaseProtocol

    @Binds
    @Singleton
    abstract fun bindFeedProfileUseCase(
        impl: FeedProfileUseCase,
    ): FeedProfileUseCaseProtocol

    @Binds
    @Singleton
    abstract fun bindCourseUseCase(
        impl: CourseUseCase,
    ): CourseUseCaseProtocol

    @Binds
    @Singleton
    abstract fun bindReviewUseCase(
        impl: ReviewUseCase,
    ): ReviewUseCaseProtocol

    @Binds
    @Singleton
    abstract fun bindLectureUseCase(
        impl: LectureUseCase,
    ): LectureUseCaseProtocol

    @Binds
    @Singleton
    abstract fun bindPostTranslationUseCase(
        impl: PostTranslationUseCase,
    ): PostTranslationUseCaseProtocol

    @Binds
    @Singleton
    abstract fun bindSummarizationUseCase(
        impl: SummarizationUseCase,
    ): SummarizationUseCaseProtocol

    @Binds
    @Singleton
    abstract fun bindTextProcessingDelegate(
        impl: TextProcessingDelegate,
    ): TextProcessingProtocol

}

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    @Provides
    @Singleton
    fun provideAuthenticationService(
        authRepository: AuthRepositoryProtocol,
    ): AuthenticationServiceProtocol {
        return AuthenticationService(authRepository)
    }

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = {
                context.preferencesDataStoreFile("soap_preferences")
            }
        )
    }

    @Provides
    @Singleton
    fun provideTaxiChatService(
        tokenStorage: TokenStorageProtocol,
        authUseCaseProvider: Provider<AuthUseCaseProtocol>,
    ): TaxiChatService {
        return TaxiChatService(
            tokenStorage = tokenStorage,
            authUseCaseProvider = authUseCaseProvider
        )
    }
}

@Module
@InstallIn(SingletonComponent::class)
object AuthUseCaseModule {
    @Provides
    @Singleton
    fun provideAuthUseCase(
        authenticationService: AuthenticationServiceProtocol,
        tokenStorage: TokenStorageProtocol,
        araUserRepository: AraUserRepositoryProtocol,
        feedUserRepository: FeedUserRepositoryProtocol,
        otlUserRepository: OTLUserRepositoryProtocol,
        taxiChatServiceProvider: Provider<TaxiChatService>,
        fcmUseCase: FCMUseCaseProtocol,
        widgetSyncHelper: WidgetSyncHelper
    ): AuthUseCase {

        val useCase = AuthUseCase(
            authenticationService,
            tokenStorage,
            araUserRepository,
            feedUserRepository,
            otlUserRepository,
            fcmUseCase,
            widgetSyncHelper
        )

        AuthRetryConfig.tokenRefresher = {
            useCase.refreshAccessToken(force = false)
        }

        useCase.onTokenRefresh = {
            taxiChatServiceProvider.get().reconnect()
        }

        return useCase
    }

    @Provides
    @Singleton
    fun provideAuthUseCaseProtocol(impl: AuthUseCase): AuthUseCaseProtocol = impl
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "soap_database"
        )
            .fallbackToDestructiveMigration(false)
            .build()
    }

    @Provides
    fun provideTimetableCacheDAO(database: AppDatabase): TimetableCacheDAO {
        return database.timetableCacheDao()
    }

    @Provides
    fun provideTaxiRouteCacheDAO(database: AppDatabase): TaxiRouteCacheDAO {
        return database.taxiRouteCacheDao()
    }
}
