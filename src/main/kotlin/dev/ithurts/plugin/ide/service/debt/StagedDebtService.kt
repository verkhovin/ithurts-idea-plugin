package dev.ithurts.plugin.ide.service.debt

import dev.ithurts.plugin.ide.model.Binding

class StagedDebtService {
    var stagedDebt: StagedDebt? = null

    fun stageDebt(
        bindingOptions: List<Binding>
    ) {
        val stagedDebt = stagedDebt
        if (stagedDebt != null) {
            this.stagedDebt = stagedDebt.copy(
                bindingOptions = bindingOptions
            )
        } else {
            this.stagedDebt = StagedDebt(bindingOptions)
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

    fun reset() {
        stagedDebt = null
    }

}

data class StagedDebt(
    val bindingOptions: List<Binding>,
    var title: String? = null,
    var description: String? = null
)