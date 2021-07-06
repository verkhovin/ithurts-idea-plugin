package dev.ithurts.plugin.service

import dev.ithurts.plugin.model.Tokens

class CredentialsService {
    fun saveTokens(tokens: Tokens) {
        println(tokens.accessToken)
    }

    fun getAccessToken() {

    }

    fun getRefreshToken() {

    }
}