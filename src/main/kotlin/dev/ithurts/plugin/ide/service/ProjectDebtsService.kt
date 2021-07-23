package dev.ithurts.plugin.ide.service

import com.intellij.openapi.project.Project
import dev.ithurts.plugin.model.TechDebt

class ProjectDebtsService(private val project: Project) {
    private var debts: Map<String, List<TechDebt>>? = null

    fun indexDebts(debts: Set<TechDebt>) {
        this.debts = debts.groupBy { it.filePath }
    }

    fun getDebts(filePath: String) = debts?.get(filePath) ?: emptySet()
}