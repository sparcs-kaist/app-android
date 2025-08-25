package com.example.soap.Networking

import com.example.soap.Domain.Helpers.Constants
import com.example.soap.Domain.Helpers.TokenStorage
import com.example.soap.Domain.Helpers.TokenStorageProtocol
import com.example.soap.Domain.Repositories.TaxiRoomRepository
import com.example.soap.Domain.Repositories.TaxiRoomRepositoryProtocol
import com.example.soap.Domain.Services.AuthenticationService
import com.example.soap.Domain.Services.AuthenticationServiceProtocol
import com.example.soap.Domain.Usecases.AuthUseCase
import com.example.soap.Domain.Usecases.AuthUseCaseProtocol
import com.example.soap.Networking.RetrofitAPI.AuthApi
import com.example.soap.Networking.RetrofitAPI.Taxi.TaxiRoomApi
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    @Named("TaxiBackend")
    fun taxiBackEndURL(gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.taxiBackendURL)
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
    fun TaxiRoomService(@Named("TaxiBackend") retrofit: Retrofit): TaxiRoomApi {
        return retrofit.create(TaxiRoomApi::class.java)
    }

    @Provides
    @Singleton
    @Named("Auth")
    fun provideAuthApi(@Named("Auth") retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class StorageModule {

    @Binds
    @Singleton
    abstract fun bindTokenStorage(impl: TokenStorage): TokenStorageProtocol
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTaxiRoomRepository(
        impl: TaxiRoomRepository
    ): TaxiRoomRepositoryProtocol
}

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class AuthUseCaseModule {

    @Binds
    abstract fun bindAuthUseCase(impl: AuthUseCase): AuthUseCaseProtocol

}

@Module
@InstallIn(SingletonComponent::class)
object AuthServiceModule {

    @Provides
    @Singleton
    fun provideAuthenticationService(
        @Named("Auth") authApi: AuthApi,
        tokenStorage: TokenStorageProtocol
    ): AuthenticationServiceProtocol {
        return AuthenticationService(authApi, tokenStorage)
    }

}