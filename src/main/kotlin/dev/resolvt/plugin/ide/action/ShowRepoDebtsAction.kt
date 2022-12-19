package dev.resolvt.plugin.ide.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.wm.ToolWindowManager
import dev.resolvt.plugin.ide.service.debt.DebtBrowserService

class ShowRepoDebtsAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val debtBrowserService = project.service<DebtBrowserService>()
        debtBrowserService.showRepoDebts()
        ToolWindowManager.getInstance(project).getToolWindow("Resolvt")!!.activate(null)
    }
}