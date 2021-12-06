package dev.ithurts.plugin.model


data class DebtDTO(
    val id: Long,
    val title: String,
    val description: String,
    val status: DebtStatus,
    val filePath: String,
    val startLine: Int,
    val endLine: Int,
    val votes: Int,
    val accountDTO: AccountDTO
)