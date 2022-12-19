package dev.resolvt.plugin.client

import com.fasterxml.jackson.annotation.JsonProperty

class ResolvtError(
    @JsonProperty("reason") val reason: String,
    @JsonProperty("message") val message: String
)