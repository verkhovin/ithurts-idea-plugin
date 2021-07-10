package dev.ithurts.plugin.client

import com.fasterxml.jackson.annotation.JsonProperty

class ItHurtsError(
    @JsonProperty("status") val status: String,
    @JsonProperty("reason") val reason: String,
    @JsonProperty("message") val message: String
)