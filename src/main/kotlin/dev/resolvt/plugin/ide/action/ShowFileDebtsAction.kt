package dev.resolvt.plugin.ide.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.wm.ToolWindowManager
import dev.resolvt.plugin.common.FileUtils
import dev.resolvt.plugin.ide.service.debt.DebtBrowserService

class ShowFileDebtsAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val debtBrowserService = project.service<DebtBrowserService>()
        val virtualFile = FileEditorManager.getInstance(project).selectedEditor?.file ?: return
        val relativePath = FileUtils.getRelativePath(project, virtualFile)!!
        debtBrowserService.showFileDebts(relativePath)
        ToolWindowManager.getInstance(project).getToolWindow("Resolvt")!!.activate(null)
    }
}