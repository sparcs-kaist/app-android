package com.example.soap.Networking

import com.example.soap.Domain.Helpers.Constants
import com.example.soap.Domain.Helpers.TokenStorage
import com.example.soap.Domain.Helpers.TokenStorageProtocol
import com.example.soap.Domain.Helpers.UserStorage
import com.example.soap.Domain.Helpers.UserStorageProtocol
import com.example.soap.Domain.Repositories.Ara.AraBoardRepository
import com.example.soap.Domain.Repositories.Ara.AraBoardRepositoryProtocol
import com.example.soap.Domain.Repositories.Ara.AraCommentRepository
import com.example.soap.Domain.Repositories.Ara.AraCommentRepositoryProtocol
import com.example.soap.Domain.Repositories.Ara.AraUserRepository
import com.example.soap.Domain.Repositories.Ara.AraUserRepositoryProtocol
import com.example.soap.Domain.Repositories.Feed.FeedCommentRepository
import com.example.soap.Domain.Repositories.Feed.FeedCommentRepositoryProtocol
import com.example.soap.Domain.Repositories.Feed.FeedImageRepository
import com.example.soap.Domain.Repositories.Feed.FeedImageRepositoryProtocol
import com.example.soap.Domain.Repositories.Feed.FeedPostRepository
import com.example.soap.Domain.Repositories.Feed.FeedPostRepositoryProtocol
import com.example.soap.Domain.Repositories.Feed.FeedUserRepository
import com.example.soap.Domain.Repositories.Feed.FeedUserRepositoryProtocol
import com.example.soap.Domain.Repositories.Taxi.TaxiReportRepository
import com.example.soap.Domain.Repositories.Taxi.TaxiReportRepositoryProtocol
import com.example.soap.Domain.Repositories.Feed.FeedCommentRepository
import com.example.soap.Domain.Repositories.Feed.FeedCommentRepositoryProtocol
import com.example.soap.Domain.Repositories.Feed.FeedImageRepository
import com.example.soap.Domain.Repositories.Feed.FeedImageRepositoryProtocol
import com.example.soap.Domain.Repositories.Feed.FeedPostRepository
import com.example.soap.Domain.Repositories.Feed.FeedPostRepositoryProtocol
import com.example.soap.Domain.Repositories.Feed.FeedUserRepository
import com.example.soap.Domain.Repositories.Feed.FeedUserRepositoryProtocol
import com.example.soap.Domain.Repositories.Taxi.TaxiRoomRepository
import com.example.soap.Domain.Repositories.Taxi.TaxiRoomRepositoryProtocol
import com.example.soap.Domain.Repositories.Taxi.TaxiUserRepository
import com.example.soap.Domain.Repositories.Taxi.TaxiUserRepositoryProtocol
import com.example.soap.Domain.Services.AuthenticationService
import com.example.soap.Domain.Services.AuthenticationServiceProtocol
import com.example.soap.Domain.Usecases.AuthUseCase
import com.example.soap.Domain.Usecases.AuthUseCaseProtocol
import com.example.soap.Domain.Usecases.TaxiChatUseCase
import com.example.soap.Domain.Usecases.TaxiChatUseCaseProtocol
import com.example.soap.Domain.Usecases.UserUseCase
import com.example.soap.Domain.Usecases.UserUseCaseProtocol
import com.example.soap.Networking.RetrofitAPI.Ara.AraBoardApi
import com.example.soap.Networking.RetrofitAPI.Ara.AraCommentApi
import com.example.soap.Networking.RetrofitAPI.Ara.AraUserApi
import com.example.soap.Networking.RetrofitAPI.AuthApi
import com.example.soap.Networking.RetrofitAPI.Feed.FeedCommentApi
import com.example.soap.Networking.RetrofitAPI.Feed.FeedImageApi
import com.example.soap.Networking.RetrofitAPI.Feed.FeedPostApi
import com.example.soap.Networking.RetrofitAPI.Feed.FeedUserApi
import com.example.soap.Networking.RetrofitAPI.Taxi.TaxiChatApi
import com.example.soap.Networking.RetrofitAPI.Taxi.TaxiReportApi
import com.example.soap.Networking.RetrofitAPI.Taxi.TaxiRoomApi
import com.example.soap.Networking.RetrofitAPI.Taxi.TaxiUserApi
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton


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