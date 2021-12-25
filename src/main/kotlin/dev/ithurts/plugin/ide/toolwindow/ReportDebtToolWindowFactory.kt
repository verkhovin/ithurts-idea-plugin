package dev.ithurts.plugin.ide.toolwindow

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import dev.ithurts.plugin.ide.service.debt.StagedDebtService

class ReportDebtToolWindowFactory: ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        toolWindow.contentManager.addContent(ReportDebtToolWindow(project).getContent())
    }
}