package dev.resolvt.plugin.ide.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.credentialStore.generateServiceName
import com.intellij.ide.passwordSafe.PasswordSafe
import dev.resolvt.plugin.client.model.Tokens


class CredentialsService {

    private val objectMapper: ObjectMapper = ObjectMapper().also { it.registerKotlinModule() }

    companion object {
        private const val TOKENS_KEY = "Tokens"
        private const val CREDENTIALS_USERNAME = "tokens"
    }

    fun saveTokens(tokens: Tokens) {
        val tokensAttribute = createCredentialAttributes(TOKENS_KEY)
        val tokensJson = objectMapper.writeValueAsString(tokens)
        val credentials = Credentials(CREDENTIALS_USERNAME, tokensJson)
        PasswordSafe.instance.set(tokensAttribute, credentials)
    }

    fun clearTokens() {
        val tokensAttribute = createCredentialAttributes(TOKENS_KEY)
        PasswordSafe.instance.set(tokensAttribute, null)
    }

    fun getTokens(): Tokens? {
        val tokensJson = PasswordSafe.instance.getPassword(createCredentialAttributes(TOKENS_KEY))
            ?: return null
        return objectMapper.readValue(tokensJson)
    }

    fun getAccessToken() = getTokens()?.accessToken!!

    fun getRefreshToken() = getTokens()?.refreshToken!!

    private fun createCredentialAttributes(key: String) = CredentialAttributes(generateServiceName("Resolvt", key))
}
