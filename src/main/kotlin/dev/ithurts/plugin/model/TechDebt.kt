package dev.ithurts.plugin.model

import com.fasterxml.jackson.annotation.JsonProperty

class TechDebt(
    @JsonProperty("id")
    val id: Long,
    @JsonProperty("title")
    val title: String,
    @JsonProperty("description")
    val description: String,
    @JsonProperty("file_path")
    val filePath: String,
    @JsonProperty("start_line")
    val startLine: Int,
    @JsonProperty("end_line")
    val endLine:Int
)