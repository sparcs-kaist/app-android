package com.example.soap.Domain.Usecases

import android.app.Activity
import kotlinx.coroutines.flow.Flow


interface AuthUseCaseProtocol {
    val isAuthenticatedFlow: Flow<Boolean>

    @Throws(Exception::class)
    suspend fun signIn(activity: Activity)

    @Throws(Exception::class)
    suspend fun signOut()

    fun getAccessToken(): String?

    @Throws(Exception::class)
    suspend fun getValidAccessToken(): String

    @Throws(Exception::class)
    suspend fun refreshAccessTokenIfNeeded()
}