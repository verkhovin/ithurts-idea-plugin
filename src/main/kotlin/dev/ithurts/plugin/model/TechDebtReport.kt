package dev.ithurts.plugin.model

import com.fasterxml.jackson.annotation.JsonAutoDetect

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class TechDebtReport(
    val title: String,
    val description: String,
    val remoteUrl: String,
    val filePath: String,
    val startLine: Int,
    val endLine: Int
)