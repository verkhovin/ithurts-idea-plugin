package dev.ithurts.plugin.common

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import dev.ithurts.plugin.ide.toolwindow.ReportDebtToolWindowOld

object UiUtils {
    fun rerenderReportDebtToolWindow(project: Project) {
        val toolWindow = getReportDebtToolWindow(project)
        val contentManager = toolWindow.contentManager
        contentManager.removeAllContents(true)
        val reportDebtToolWindow = ReportDebtToolWindowOld(project)
        contentManager.addContent(reportDebtToolWindow.getContent())
        toolWindow.activate(null)
    }

    fun hideReportDebtToolWindow(project: Project) {
        val toolWindow = getReportDebtToolWindow(project)
        toolWindow.hide(null)
    }


    fun getReportDebtToolWindow(project: Project) = ToolWindowManager.getInstance(project).getToolWindow("Report Technical Debt")!!
}