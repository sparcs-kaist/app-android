package org.sparcs.App.Domain.Helpers

import java.util.Date


interface TokenStorageProtocol {
    fun save(accessToken: String, refreshToken: String)
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    fun isTokenExpired(): Boolean
    fun getTokenExpirationDate(): Date?
    fun clearTokens()
}