package dev.ithurts.plugin.common

object Consts {
    private const val baseUrl = "https://ithurts.dev"
    //    private const val baseUrl = "http://localhost:8080"
    private const val apiUrl = "$baseUrl/api"
    const val authUrl = "${baseUrl}/plugins/auth/code"
    const val accessTokenUrl = "${apiUrl}/auth/access-token"
    const val meUrl = "${apiUrl}/me"
    const val debtsUrl = "${apiUrl}/debts"

    const val PROJECT_REMOTE_PROPERTY_KEY = "dev.ithurts.idea-integration.projectRemote"
    const val SAVED_TITLE_PROPERTY_KEY = "dev.ithurts.idea-integration.title"
    const val SAVED_DESCRIPTION_PROPERTY_KEY = "dev.ithurts.idea-integration.description"
}