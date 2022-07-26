package dev.ithurts.plugin.ide.model

data class DebtView(
    val id: String,
    val title: String,
    val description: String,
    val status: DebtStatus,
    val bindings: List<Binding>,
    val votes: Int,
    val voted: Boolean,
    val reporter: Account?,
    val createdAt: String,
    val updatedAt: String,
    val cost: Int,
    val hasBindingTrackingLost: Boolean
)