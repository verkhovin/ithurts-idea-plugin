package dev.ithurts.plugin.client

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.Messages
import dev.ithurts.plugin.common.Consts
import dev.ithurts.plugin.model.Me
import dev.ithurts.plugin.model.Tokens
import dev.ithurts.plugin.ide.service.CredentialsService
import dev.ithurts.plugin.model.DebtDTO
import dev.ithurts.plugin.model.TechDebtReport
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.EMPTY_REQUEST
import java.io.IOException

object ItHurtsClient {
    private val mapper = ObjectMapper().registerModule(KotlinModule())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    private val client = OkHttpClient.Builder().addInterceptor(
        ItHurtsTokenExpiredInterceptor(this::refreshTokens)
    ).addInterceptor {
        it.proceed(it.request().newBuilder().addHeader("Accept", "application/json").build())
    }.build()

    fun getTokens(
        authCode: String,
        codeVerifier: String,
        callback: (Tokens) -> Unit,
        errorCallback: (ItHurtsError) -> Unit
    ) {
        val url = Consts.accessTokenUrl.toHttpUrl().newBuilder()
            .addQueryParameter("authorizationCode", authCode)
            .addQueryParameter("codeVerifier", codeVerifier)
            .addQueryParameter("grantType", "authorization_code")
            .build()
        val request = Request.Builder().url(url).method("POST", EMPTY_REQUEST).build()
        executeAsync(request, callback, errorCallback)
    }

    fun me(callback: (Me) -> Unit, errorCallback: (ItHurtsError) -> Unit) {
        val accessToken = service<CredentialsService>().getAccessToken()
        val request = Request.Builder().url(Consts.meUrl)
            .addHeader("Authorization", "Bearer $accessToken")
            .build()
        executeAsync(request, callback, errorCallback)
    }

    fun report(techDebtReport: TechDebtReport, callback: () -> Unit, errorCallback: (ItHurtsError) -> Unit) {
        val accessToken = service<CredentialsService>().getAccessToken()
        val body = mapper.writeValueAsString(techDebtReport)
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder().url(Consts.debtsUrl).method("POST", body)
            .addHeader("Authorization", "Bearer $accessToken")
            .build()
        executeAsync(request, { _: Any? -> callback() }, errorCallback)
    }

    fun getDebtsForRepo(
        remoteUrl: String,
        callback: (debts: Set<DebtDTO>) -> Unit,
        errorCallback: (ItHurtsError) -> Unit
    ) {
        val accessToken = service<CredentialsService>().getAccessToken()
        val url = Consts.debtsUrl.toHttpUrl().newBuilder()
            .addQueryParameter("remoteUrl", remoteUrl)
            .build()
        val request = Request.Builder().url(url)
            .addHeader("Authorization", "Bearer $accessToken")
            .build()
        executeAsync(request, callback, object : TypeReference<Set<DebtDTO>>() {}, errorCallback)
    }

    private fun refreshTokens() {
        val credentialsService = service<CredentialsService>()
        val refreshToken = credentialsService.getRefreshToken()
        val url = Consts.accessTokenUrl.toHttpUrl().newBuilder()
            .addQueryParameter("grantType", "refresh_token")
            .addQueryParameter("refreshToken", refreshToken)
            .build()
        val call = OkHttpClient().newCall(
            Request.Builder().url(url)
                .method("POST", EMPTY_REQUEST)
                .addHeader("Accept", "application/json")
                .build()
        )
        handleResponse(
            call.execute(),
            { tokens: Tokens -> credentialsService.saveTokens(tokens) },
            { credentialsService.clearTokens() }
        )
    }

    private inline fun <reified T> executeAsync(
        request: Request,
        crossinline callback: (T) -> Unit,
        crossinline errorCallback: (ItHurtsError) -> Unit = this::handleError
    ) {
        client.newCall(request).enqueue(handle { _, response -> handleResponse(response, callback, errorCallback) })
    }

    private inline fun <reified T> executeAsync(
        request: Request,
        crossinline callback: (T) -> Unit,
        responseTypeReference: TypeReference<T>,
        crossinline errorCallback: (ItHurtsError) -> Unit = this::handleError
    ) {
        client.newCall(request)
            .enqueue(handle { _, response -> handleResponse(response, callback, errorCallback, responseTypeReference) })
    }

    private inline fun <reified T> handleResponse(
        response: Response, successCallback: (T) -> Unit,
        errorCallback: (ItHurtsError) -> Unit, typeReference: TypeReference<T>? = null
    ) {
        if (response.code >= 400) {
            val error: ItHurtsError = try {
                mapper.readValue(response.body!!.string(), ItHurtsError::class.java)
            } catch (e: JsonMappingException) {
                throw Exception("Unsupported response from It Hurts", e)
            }
            errorCallback(error)
        } else {
            val body = response.body!!
            if (body.contentLength() == 0L) {
                successCallback(null as T)
                return
            }
            val entity = if (typeReference == null) {
                mapper.readValue(response.body!!.string(), T::class.java)
            } else {
                mapper.readValue(response.body!!.string(), typeReference)
            }
            successCallback(entity)
        }
    }

    private fun handleError(e: IOException) {
        e.printStackTrace() // FIXME write full log and make a hint
        ApplicationManager.getApplication().invokeLater {
            Notifications.Bus.notify(
                Notification(
                    "",
                    "Failed to connect to It Hurts",
                    "Error: ${e.javaClass.simpleName} ${e.message}",
                    NotificationType.ERROR
                )
            )
        }
    }

    private fun handleError(error: ItHurtsError) {
        throw Exception(error.message)
    }

    private fun handle(onSuccess: (Call, Response) -> Unit) = object : Callback {
        override fun onFailure(call: Call, e: IOException) = handleError(e)

        override fun onResponse(call: Call, response: Response) = onSuccess(call, response)
    }
}