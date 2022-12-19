package dev.resolvt.plugin.ide.service.debt

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import dev.resolvt.plugin.ide.model.Binding
import dev.resolvt.plugin.ide.model.EditableDebt

class StagedDebtService(private val project: Project) {
    var stagedDebt: EditableDebt? = null
    var stageMode: StageMode = StageMode.CREATE

    fun stageBinding(
        bindingOption: Binding
    ) {
        val stagedDebt = stagedDebt
        if (stagedDebt != null) {
            this.stagedDebt?.bindings?.add(bindingOption)
        } else {
            this.stagedDebt = EditableDebt(bindings = mutableListOf(bindingOption))
        }
    }

    fun editDebt(debtId: String) {
        val debtStorageService = project.service<DebtStorageService>()
        val debt = debtStorageService.getDebt(debtId) ?: throw IllegalArgumentException("Debt with id $debtId does not exist")
        stageMode = StageMode.EDIT
        stagedDebt = EditableDebt(debt.id, debt.title, debt.description, debt.bindings.toMutableList())
    }

    fun cancelEditing() {
        stagedDebt = null
        stageMode = StageMode.CREATE
    }


    fun removeBinding(bindingOption: Binding) {
        this.stagedDebt?.bindings?.remove(bindingOption)
    }

    fun reset() {
        stagedDebt = null
        stageMode = StageMode.CREATE
    }

}

enum class StageMode {
    CREATE,
    EDIT
}