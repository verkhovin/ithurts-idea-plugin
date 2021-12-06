package dev.ithurts.plugin.ide.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.wm.ToolWindowManager
import dev.ithurts.plugin.client.ItHurtsClient
import dev.ithurts.plugin.common.FileUtils
import dev.ithurts.plugin.common.RepoUtils
import dev.ithurts.plugin.ide.service.debt.DebtBrowserService
import dev.ithurts.plugin.ide.service.debt.DebtEditorDisplayService
import dev.ithurts.plugin.ide.service.debt.DebtStorageService

class ShowFileDebtsAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val debtBrowserService = project.service<DebtBrowserService>()
        val virtualFile = FileEditorManager.getInstance(project).selectedEditor?.file ?: return
        val relativePath = FileUtils.getRelativePath(project, virtualFile)
        debtBrowserService.showFileDebts(relativePath)
        ToolWindowManager.getInstance(project).getToolWindow("It Hurts")!!.activate(null)
    }
}