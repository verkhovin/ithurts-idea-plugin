package dev.resolvt.plugin.client

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import dev.resolvt.plugin.client.model.*
import dev.resolvt.plugin.common.Consts
import dev.resolvt.plugin.ide.service.CredentialsService
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.EMPTY_REQUEST
import org.slf4j.LoggerFactory
import java.io.IOException


//TODO this one is absolutely cursed, to be reimplemented
class ResolvtClient {
    private val mapper = jacksonObjectMapper()
        .findAndRegisterModules()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    private val client = OkHttpClient.Builder().addInterceptor(
        ResolvtTokenExpiredInterceptor(this::refreshTokens)
    ).addInterceptor {
        it.proceed(it.request().newBuilder().addHeader("Accept", "application/json").build())
    }.build()

    private val log = LoggerFactory.getLogger(ResolvtClient::class.java)

    fun getTokens(
        host: String,
        authCode: String,
        codeVerifier: String,
        callback: (Tokens) -> Unit,
        errorCallback: (ResolvtError) -> Unit
    ) {
        val url = (host + Consts.accessTokenUrl).toHttpUrl().newBuilder()
            .addQueryParameter("authorizationCode", authCode)
            .addQueryParameter("codeVerifier", codeVerifier)
            .addQueryParameter("grantType", "authorization_code")
            .build()
        val request = Request.Builder().url(url).method("POST", EMPTY_REQUEST).build()
        executeAsync(request, callback, errorCallback)
    }

    fun me(callback: (Me) -> Unit, errorCallback: (ResolvtError) -> Unit) {
        val accessToken = service<CredentialsService>().getAccessToken()
        val request = Request.Builder().url(withHost(Consts.meUrl))
            .addHeader("Authorization", "Bearer $accessToken")
            .build()
        executeAsync(request, callback, errorCallback)
    }

    fun report(techDebtReport: TechDebtReport, callback: () -> Unit, errorCallback: (ResolvtError) -> Unit) {
        val accessToken = service<CredentialsService>().getAccessToken()
        val body = mapper.writeValueAsString(techDebtReport)
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder().url(withHost(Consts.debtsUrl)).method("POST", body)
            .addHeader("Authorization", "Bearer $accessToken")
            .build()
        executeAsync(request, { _: Any? -> callback() }, errorCallback)
    }

    fun editDebt(debtId: String, techDebtReport: TechDebtReport, callback: () -> Unit, errorCallback: (ResolvtError) -> Unit) {
        val accessToken = service<CredentialsService>().getAccessToken()
        val body = mapper.writeValueAsString(techDebtReport)
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder().url("${withHost(Consts.debtsUrl)}/$debtId").method("PUT", body)
            .addHeader("Authorization", "Bearer $accessToken")
            .build()
        executeAsync(request, { _: Any? -> callback() }, errorCallback)
    }

    fun vote(debtId: String, callback: () -> Unit, errorCallback: (ResolvtError) -> Unit) {
        val accessToken = service<CredentialsService>().getAccessToken()
        val request = Request.Builder().url("${withHost(Consts.debtsUrl)}/$debtId/vote").method("POST", EMPTY_REQUEST)
            .addHeader("Authorization", "Bearer $accessToken")
            .build()
        executeAsync(request, { _: Any? -> callback() }, errorCallback)
    }

    fun downVote(debtId: String, callback: () -> Unit, errorCallback: (ResolvtError) -> Unit) {
        val accessToken = service<CredentialsService>().getAccessToken()
        val request = Request.Builder().url("${withHost(Consts.debtsUrl)}/$debtId/downVote").method("POST", EMPTY_REQUEST)
            .addHeader("Authorization", "Bearer $accessToken")
            .build()
        executeAsync(request, { _: Any? -> callback() }, errorCallback)
    }

    fun getDebtsForRepo(
        remoteUrl: String,
        callback: (debts: Set<DebtDto>) -> Unit,
    ) {
        val accessToken = service<CredentialsService>().getAccessToken()
        val url = withHost(Consts.debtsUrl).toHttpUrl().newBuilder()
            .addQueryParameter("remoteUrl", remoteUrl)
            .build()
        val request = Request.Builder().url(url)
            .addHeader("Authorization", "Bearer $accessToken")
            .build()
        executeAsync(request, callback, object : TypeReference<Set<DebtDto>>() {}) {
            log.error("Failed to get debts for repo $remoteUrl", it.toString())
        }
    }

    fun getRepository(
        remoteUrl: String,
        callback: (debts: RepositoryDto) -> Unit,
    ) {
        val accessToken = service<CredentialsService>().getAccessToken()
        val url = withHost(Consts.repositoriesUrl).toHttpUrl().newBuilder()
            .addQueryParameter("remoteUrl", remoteUrl)
            .build()
        val request = Request.Builder().url(url)
            .addHeader("Authorization", "Bearer $accessToken")
            .build()
        executeAsync(request, callback) {
            log.error("Failed to get debts for repo $remoteUrl", it.toString())
        }
    }

    private fun refreshTokens() {
        val credentialsService = service<CredentialsService>()
        val refreshToken = credentialsService.getRefreshToken()
        val url = withHost(Consts.accessTokenUrl).toHttpUrl().newBuilder()
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
            { tokens: Tokens -> credentialsService.updateTokens(tokens) },
            { credentialsService.clearTokens() }
        )
    }

    private fun withHost(url: String): String = service<CredentialsService>().getHost() + url

    private inline fun <reified T> executeAsync(
        request: Request,
        crossinline callback: (T) -> Unit,
        crossinline errorCallback: (ResolvtError) -> Unit = this::handleError
    ) {
        client.newCall(request).enqueue(handle { _, response -> handleResponse(response, callback, errorCallback) })
    }

    private inline fun <reified T> executeAsync(
        request: Request,
        crossinline callback: (T) -> Unit,
        responseTypeReference: TypeReference<T>,
        crossinline errorCallback: (ResolvtError) -> Unit = this::handleError
    ) {
        client.newCall(request)
            .enqueue(handle { _, response -> handleResponse(response, callback, errorCallback, responseTypeReference) })
    }

    private inline fun <reified T> handleResponse(
        response: Response, successCallback: (T) -> Unit,
        errorCallback: (ResolvtError) -> Unit, typeReference: TypeReference<T>? = null
    ) {
        if (response.code >= 400) {
            val error: ResolvtError = try {
                mapper.readValue(response.body!!.string(), ResolvtError::class.java)
            } catch (e: JsonMappingException) {
                throw Exception("Unsupported response from Resolvt", e)
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
                    "Failed to connect to Resolvt",
                    "Error: ${e.javaClass.simpleName} ${e.message}",
                    NotificationType.ERROR
                )
            )
        }
    }

    private fun handleError(error: ResolvtError) {
        ApplicationManager.getApplication().invokeLater {
            Notifications.Bus.notify(
                Notification(
                    "",
                    "Got an error from Resolvt",
                    error.message,
                    NotificationType.ERROR
                )
            )
        }
    }

    private fun handle(onSuccess: (Call, Response) -> Unit) = object : Callback {
        override fun onFailure(call: Call, e: IOException) = handleError(e)

        override fun onResponse(call: Call, response: Response) = onSuccess(call, response)
    }
}