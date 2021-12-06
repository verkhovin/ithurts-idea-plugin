package dev.ithurts.plugin.ide.service

import com.intellij.openapi.project.Project
import dev.ithurts.plugin.model.DebtDTO

class DebtStorageService(private val project: Project) {
    private var debts: Map<String, List<DebtDTO>>? = null

    fun indexDebts(debts: Set<DebtDTO>) {
        this.debts = debts.groupBy { it.filePath }
    }

    fun getDebts(): Map<String, List<DebtDTO>> {
        return debts!!
    }

    fun  getDebts(filePath: String) = debts?.get(filePath) ?: emptyList()

    fun getDebts(filePath: String, lineNumber: Int) = getDebts(filePath).filter { it.startLine == lineNumber }
}