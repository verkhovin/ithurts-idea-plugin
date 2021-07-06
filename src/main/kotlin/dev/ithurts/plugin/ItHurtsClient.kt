package dev.ithurts.plugin

import com.fasterxml.jackson.databind.ObjectMapper
import dev.ithurts.plugin.common.Consts
import dev.ithurts.plugin.model.Tokens
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.internal.EMPTY_REQUEST
import java.io.IOException

object ItHurtsClient {
    private val mapper = ObjectMapper()
    fun getTokens(authCode: String, codeVerifier: String, callback: (Tokens) -> Unit) {
        val url = Consts.accessTokenUrl.toHttpUrl().newBuilder()
                .addQueryParameter("authorization_code", authCode)
                .addQueryParameter("code_verifier", codeVerifier)
                .addQueryParameter("grant_type", "authorization_code")
                .build()
        val request = Request.Builder().url(url).method("POST", EMPTY_REQUEST).build()
        OkHttpClient().newCall(request).enqueue(handle { call, response ->
            if (response.code >= 400) {
                handleError(call, response)
            } else {
                val tokens = mapper.readValue(response.body!!.string(), Tokens::class.java)
                callback(tokens)
            }
        })
    }

    private fun handleError(call: Call, e: IOException) {
        e.printStackTrace() // FIXME write full log and make a hint
    }

    private fun handleError(call: Call, response: Response) {
        println(response.body.toString())
    }

    private fun handle(onSuccess: (Call, Response) -> Unit) = object: Callback {
        override fun onFailure(call: Call, e: IOException) = handleError(call, e)

        override fun onResponse(call: Call, response: Response) = onSuccess(call, response)
    }
}