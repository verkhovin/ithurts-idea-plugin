package dev.ithurts.plugin.common

object Consts {
    private const val baseUrl = "http://localhost:4000"
    private const val apiUrl = "$baseUrl/api"
    const val authUrl = "${baseUrl}/plugins/auth/code"
    const val accessTokenUrl = "${baseUrl}/api/auth/access_token"
    const val meUrl = "${apiUrl}/me"
}