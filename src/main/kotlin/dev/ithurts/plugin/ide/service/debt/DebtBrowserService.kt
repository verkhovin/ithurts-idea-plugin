package dev.ithurts.plugin.ide.service.debt

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefBrowserBase
import com.intellij.ui.jcef.JBCefJSQuery
import com.intellij.util.ui.UIUtil
import dev.ithurts.plugin.common.FileUtils
import dev.ithurts.plugin.model.DebtDTO
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import java.io.StringWriter


class DebtBrowserService(private val project: Project) {
    val browser = JBCefBrowser()

    fun showDebts(filePath: String, lineNumber: Int) {
        val debtStorageService = project.service<DebtStorageService>()
        val debts = debtStorageService.getDebts(filePath, lineNumber)
        browser.loadHTML(buildPage(debts, ShowLevel.LINE))

        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("It Hurts")!!
        toolWindow.activate(null)
    }

    fun showRepoDebts() {
        val debtStorageService = project.service<DebtStorageService>()
        val debts = debtStorageService.getDebts().flatMap { it.value }
        browser.loadHTML(buildPage(debts, ShowLevel.REPO))
    }

    fun showFileDebts(filePath: String) {
        val debtStorageService = project.service<DebtStorageService>()
        val debts = debtStorageService.getDebts(filePath)
        browser.loadHTML(buildPage(debts, ShowLevel.FILE))
    }

    private fun buildPage(debts: List<DebtDTO>, showLevel: ShowLevel): String {
        val templateEngine = TemplateEngine()
        val templateResolver = ClassLoaderTemplateResolver()
        templateResolver.setTemplateMode("HTML")
        templateEngine.setTemplateResolver(templateResolver)
        val context = Context()

        addCallback(context, debts, "navigateQuery", this::navigateToCode)
        addCallback(context, debts, "voteQuery", this::vote)
        addCallback(context, "showRepoDebtsQuery") { showRepoDebts() }
        addCallback(context, "showFileDebtsQuery") { showFileDebts(debts[0].filePath) }

        context.setVariable("debts", debts)
        context.setVariable("level", showLevel)
        context.setVariable("dark", UIUtil.isUnderDarcula())
        val stringWriter = StringWriter()
        templateEngine.process("templates/debts.html", context, stringWriter)
        return stringWriter.toString()
    }

    private fun navigateToCode(debtId: Long, debts: List<DebtDTO>) {
        val debt = debts.find { it.id == debtId }!!
        val file = FileUtils.virtualFileByPath(project, debt.filePath)
        ApplicationManager.getApplication().invokeLater {
            FileEditorManager.getInstance(project).openTextEditor(
                OpenFileDescriptor(project, file, debt.startLine - 1, 0), true
            )
        }
    }

    private fun vote(debtId: Long, debts: List<DebtDTO>) {
        // TODO: implement voting
    }

    private fun addCallback(
        context: Context,
        callbackName: String,
        callback: (String) -> Unit
    ) {
        val query = JBCefJSQuery.create(browser as JBCefBrowserBase)
        query.addHandler { param ->
            callback(param).let { null }
        }
        context.setVariable(callbackName, query)
    }

    private fun addCallback(
        context: Context,
        debts: List<DebtDTO>,
        callbackName: String,
        callback: (Long, List<DebtDTO>) -> Unit
    ) {
        addCallback(context, callbackName) { debtId ->
            callback(debtId.toLong(), debts)
        }
    }
}

enum class ShowLevel {
    LINE,
    FILE,
    REPO
}