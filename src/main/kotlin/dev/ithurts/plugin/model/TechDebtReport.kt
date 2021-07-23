package dev.ithurts.plugin.model

import com.fasterxml.jackson.annotation.JsonProperty

class TechDebtReport(
    @JsonProperty("title")
    val title: String,
    @JsonProperty("description")
    val description: String,
    @JsonProperty("remote_url")
    val remoteUrl: String,
    @JsonProperty("start_line")
    val startLine: Int,
    @JsonProperty("end_line")
    val endLine:Int
)