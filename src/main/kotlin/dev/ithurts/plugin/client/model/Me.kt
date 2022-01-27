package dev.ithurts.plugin.client.model

import com.fasterxml.jackson.annotation.JsonProperty

class Me(
    @JsonProperty("id") val id: Long,
    @JsonProperty("name") val name: String
)
