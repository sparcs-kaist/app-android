package com.example.soap.Networking

import com.example.soap.Domain.Repositories.TaxiRoomRepository
import com.example.soap.Domain.Repositories.TaxiRoomRepositoryProtocol
import com.example.soap.Domain.Repositories.TaxiRoomService
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun taxiBackEndURL(gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://taxi.sparcs.org/api/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun TaxiRoomService(retrofit: Retrofit): TaxiRoomService {
        return retrofit.create(TaxiRoomService::class.java)
    }
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