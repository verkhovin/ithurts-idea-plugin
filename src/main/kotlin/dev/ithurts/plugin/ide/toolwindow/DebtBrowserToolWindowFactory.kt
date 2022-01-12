package dev.ithurts.plugin.ide.toolwindow

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import dev.ithurts.plugin.ide.service.debt.DebtBrowserService

class DebtBrowserToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val debtBrowserService = project.service<DebtBrowserService>()
        val browser = debtBrowserService.browser

        val contentFactory = ContentFactory.SERVICE.getInstance()
        val content: Content = contentFactory.createContent(browser.component, "", false)
        val contentManager = toolWindow.contentManager
        contentManager.addContent(content)
    }
}