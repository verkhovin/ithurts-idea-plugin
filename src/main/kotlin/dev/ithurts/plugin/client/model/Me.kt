package dev.ithurts.plugin.client.model

import com.fasterxml.jackson.annotation.JsonProperty

class Me(
    @JsonProperty("id") val id: String,
    @JsonProperty("name") val name: String
)
