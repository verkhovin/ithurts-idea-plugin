package dev.ithurts.plugin.ide.service.debt

import dev.ithurts.plugin.ide.service.binding.Binding

class StagedDebtService {
    var stagedDebt: StagedDebt? = null

    fun stageDebt(
        filePath: String,
        startLine: Int,
        endLine: Int,
        bindingOptions: List<Binding>
    ) {
        val stagedDebt = stagedDebt
        if (stagedDebt != null) {
            this.stagedDebt = stagedDebt.copy(
                filePath = filePath,
                startLine = startLine,
                endLine = endLine,
                bindingOptions = bindingOptions
            )
        } else {
            this.stagedDebt = StagedDebt(filePath, startLine, endLine, bindingOptions)
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
    val filePath: String,
    val startLine: Int,
    val endLine: Int,
    val bindingOptions: List<Binding>,
    var title: String? = null,
    var description: String? = null
)