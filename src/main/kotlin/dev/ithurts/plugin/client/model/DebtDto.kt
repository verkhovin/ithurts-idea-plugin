package dev.ithurts.plugin.client.model

data class DebtDto(
    val id: String,
    val title: String,
    val description: String,
    val status: DebtStatus,
    val filePath: String,
    val startLine: Int,
    val endLine: Int,
    var votes: Int,
    val voted: Boolean,
    val sourceLink: SourceLink,
    val repository: DebtRepositoryDto,
    val debtReporterAccount: DebtAccountDto
)

data class SourceLink(
    val url: String,
    val text: String,
)