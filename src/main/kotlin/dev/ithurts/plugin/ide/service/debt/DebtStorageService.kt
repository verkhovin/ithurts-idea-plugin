package dev.ithurts.plugin.ide.service.debt

import com.intellij.openapi.project.Project
import dev.ithurts.plugin.model.DebtDto

class DebtStorageService(private val project: Project) {
    private var debts: Map<String, List<DebtDto>>? = null

    fun indexDebts(debts: Set<DebtDto>) {
        this.debts = debts.groupBy { it.filePath }
    }

    fun getDebts(): Map<String, List<DebtDto>> {
        return debts!!
    }

    fun getDebts(filePath: String) = debts?.get(filePath) ?: emptyList()

    fun getDebts(filePath: String, lineNumber: Int) = getDebts(filePath).filter { it.startLine == lineNumber }
}