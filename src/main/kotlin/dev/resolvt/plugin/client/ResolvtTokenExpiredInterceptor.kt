package dev.resolvt.plugin.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.openapi.components.service
import dev.resolvt.plugin.ide.service.CredentialsService
import okhttp3.Interceptor
import okhttp3.Response

class ResolvtTokenExpiredInterceptor(private val refreshTokens: () -> Unit) : Interceptor {
    private val mapper = ObjectMapper()

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.code == 401) {
            val error = mapper.readValue(response.body!!.string(), ResolvtError::class.java)
            if ("token_expired" == error.reason) {
                return refreshAndRetry(response, chain)
            }
        }
        return response
    }

    private fun refreshAndRetry(response: Response, chain: Interceptor.Chain): Response {
        refreshTokens()
        val tokens = service<CredentialsService>().getTokens() ?: return response
        val request = chain.request().newBuilder()
            .removeHeader("Authorization")
            .addHeader("Authorization", "Bearer ${tokens.accessToken}")
            .build()
        return chain.proceed(request)
    }
}