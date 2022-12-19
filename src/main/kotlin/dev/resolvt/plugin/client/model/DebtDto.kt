package dev.resolvt.plugin.client.model

import dev.resolvt.plugin.ide.model.DebtStatus

data class DebtDto(
    val id: String,
    val title: String,
    val description: String,
    val status: DebtStatus,
    val bindings: List<BindingDto>,
    val votes: Int,
    val voted: Boolean,
    val repository: DebtRepositoryDto,
    val reporter: DebtAccountDto,
    val createdAt: String,
    val updatedAt: String,
    val cost: Int,
    val hasBindingTrackingLost: Boolean
)