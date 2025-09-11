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
import com.example.soap.Networking.RetrofitAPI.AuthApi
import com.example.soap.Networking.RetrofitAPI.Taxi.TaxiChatApi
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
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.inject.Named
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


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
//        val okHttpClient = OkHttpClient.Builder()
//            .addInterceptor { chain ->
//                val original = chain.request()
//                val accessToken = runBlocking { tokenStorage.getAccessToken() }
//                val newRequest = original.newBuilder()
//                    .header("Origin", "sparcsapp")
//                    .header("Content-Type", "application/json")
//                    .apply {
//                        accessToken?.let {
//                            header("Authorization", "Bearer $it")
//                        }
//                    }
//                    .build()
//                chain.proceed(newRequest)
//            }
//            .addInterceptor(HttpLoggingInterceptor().apply {
//                level = HttpLoggingInterceptor.Level.BODY
//            })
//            .build()

        return Retrofit.Builder()
            .baseUrl(Constants.araBackendURL)
            .client(getUnsafeOkHttpClient(tokenStorage)) // 개발 서버 URL
//            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    fun getUnsafeOkHttpClient(tokenStorage: TokenStorageProtocol): OkHttpClient {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())
        val sslSocketFactory = sslContext.socketFactory

        return OkHttpClient.Builder()
            .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
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
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
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
    fun provideAraBoardApi(@Named("AraBackend") retrofit: Retrofit): AraBoardApi {
        return retrofit.create(AraBoardApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAraCommentApi(@Named("AraBackend") retrofit: Retrofit): AraCommentApi {
        return retrofit.create(AraCommentApi::class.java)
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
    abstract fun bindAraBoardRepository(
        impl: AraBoardRepository
    ): AraBoardRepositoryProtocol

    @Binds
    @Singleton
    abstract fun bindAraCommentRepository(
        impl: AraCommentRepository
    ): AraCommentRepositoryProtocol
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