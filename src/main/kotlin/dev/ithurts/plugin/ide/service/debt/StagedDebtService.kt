package dev.ithurts.plugin.ide.service.debt

class StagedDebtService {
    var stagedDebt: StagedDebt? = null

    fun stageDebt(
        filePath: String,
        startLine: Int,
        endLine: Int
    ) {
        val stagedDebt = stagedDebt
        if (stagedDebt != null) {
            this.stagedDebt = stagedDebt.copy(
                filePath = filePath,
                startLine = startLine,
                endLine = endLine
            )
        } else {
            this.stagedDebt = StagedDebt(filePath, startLine, endLine)
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
    var title: String? = null,
    var description: String? = null
)