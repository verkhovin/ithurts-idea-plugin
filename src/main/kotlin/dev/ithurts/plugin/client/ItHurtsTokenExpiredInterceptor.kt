package dev.ithurts.plugin.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.openapi.components.service
import dev.ithurts.plugin.service.CredentialsService
import okhttp3.Interceptor
import okhttp3.Response

class ItHurtsTokenExpiredInterceptor(private val refreshTokens: () -> Unit) : Interceptor {
    private val mapper = ObjectMapper()

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.code == 401) {
            val error = mapper.readValue(response.body!!.string(), ItHurtsError::class.java)
            if ("token_expired" == error.reason) {
                return refreshAndRetry(response, chain)
            }
        }
        return response;
    }

    private fun refreshAndRetry(response: Response, chain: Interceptor.Chain): Response {
        refreshTokens()
        val credentialsService = service<CredentialsService>()
        if (!credentialsService.hasCredentials()) {
            return response;
        }
        val request = chain.request().newBuilder()
            .removeHeader("Authorization")
            .addHeader("Authorization", "Bearer ${credentialsService.getAccessToken()}")
            .build()
        return chain.proceed(request);
    }
}