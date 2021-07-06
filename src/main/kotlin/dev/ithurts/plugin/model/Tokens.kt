package dev.ithurts.plugin.model

import com.fasterxml.jackson.annotation.JsonProperty

class Tokens(
        @JsonProperty("access_token")
        val accessToken: String,
        @JsonProperty("refresh_token")
        val refreshToken: String
)