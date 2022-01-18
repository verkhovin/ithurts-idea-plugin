package dev.ithurts.plugin.common

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import dev.ithurts.plugin.ide.toolwindow.ReportDebtToolWindow

object UiUtils {
    fun rerenderReportDebtToolWindow(project: Project): ToolWindow {
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Report Technical Debt")!!
        val contentManager = toolWindow.contentManager
        contentManager.removeAllContents(true)
        contentManager.addContent(ReportDebtToolWindow(project).getContent())
        return toolWindow
    }
}