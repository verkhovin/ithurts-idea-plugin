package dev.resolvt.plugin.ide.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import dev.resolvt.plugin.client.ResolvtClient
import dev.resolvt.plugin.common.RepoUtils
import dev.resolvt.plugin.ide.service.ResolvtProjectInitiator
import dev.resolvt.plugin.ide.service.debt.EditorDebtDisplayService
import dev.resolvt.plugin.ide.service.debt.DebtStorageService

class FetchDebtsAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val debtStorageService = project.service<DebtStorageService>()

        val remoteUrl = RepoUtils.getRemote(project) ?: return
        service<ResolvtClient>().getDebtsForRepo(remoteUrl) {
            debtStorageService.indexDebts(it)
            ApplicationManager.getApplication().invokeLater {
                project.service<EditorDebtDisplayService>().renderDebtHighlighters()
                ResolvtProjectInitiator().runActivity(project)
            }
        }
    }
}