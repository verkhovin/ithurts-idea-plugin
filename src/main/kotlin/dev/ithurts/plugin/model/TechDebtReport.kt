package dev.ithurts.plugin.model

import com.fasterxml.jackson.annotation.JsonAutoDetect
import dev.ithurts.plugin.ide.service.binding.Binding

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class TechDebtReport(
    val title: String,
    val description: String,
    val remoteUrl: String,
    val filePath: String,
    val startLine: Int,
    val endLine: Int,
    val binding: Binding?
)