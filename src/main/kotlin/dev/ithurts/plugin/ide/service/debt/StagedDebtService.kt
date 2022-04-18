package dev.ithurts.plugin.ide.service.debt

import dev.ithurts.plugin.ide.model.Binding

class StagedDebtService {
    var stagedDebt: StagedDebt? = null

    fun stageDebt(
        bindingOption: Binding
    ) {
        val stagedDebt = stagedDebt
        if (stagedDebt != null) {
            this.stagedDebt = stagedDebt.copy(
                bindingOptions = stagedDebt.bindingOptions + bindingOption
            )
        } else {
            this.stagedDebt = StagedDebt(listOf(bindingOption))
        }
    }

    fun saveTitleAndDescription(
        title: String?,
        description: String?
    ) {
        val debtStage = this.stagedDebt ?: throw IllegalStateException("DebtStage is null")
        debtStage.title = title
        debtStage.description = description
    }

    fun removeBinding(bindingOption: Binding) {
        val debt = this.stagedDebt!!
        this.stagedDebt = debt.copy(
            bindingOptions = debt.bindingOptions - bindingOption
        )
    }

    fun reset() {
        stagedDebt = null
    }

}

data class StagedDebt(
    val bindingOptions: List<Binding>,
    var title: String? = null,
    var description: String? = null
)