package dev.ithurts.plugin.ide.service.debt

import com.intellij.ide.BrowserUtil
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
import dev.ithurts.plugin.client.ItHurtsClient
import dev.ithurts.plugin.common.FileUtils
import dev.ithurts.plugin.client.model.DebtDto
import dev.ithurts.plugin.common.Consts
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import java.io.StringWriter


class DebtBrowserService(private val project: Project) {
    val browser = JBCefBrowser()

    private var currentDebts = emptyList<DebtDto>()
    private var currentLevel: ShowLevel? = null

    fun showDebts(filePath: String, lineNumber: Int) {
        val debtStorageService = project.service<DebtStorageService>()
        currentDebts = debtStorageService.getDebts(filePath, lineNumber)
        currentLevel = ShowLevel.LINE
        render()

        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("It Hurts")!!
        toolWindow.activate(null)
    }

    fun showRepoDebts() {
        val debtStorageService = project.service<DebtStorageService>()
        currentDebts = debtStorageService.getDebts().flatMap { it.value }
        currentLevel = ShowLevel.REPO
        render()
    }

    fun showFileDebts(filePath: String) {
        val debtStorageService = project.service<DebtStorageService>()
        currentDebts = debtStorageService.getDebts(filePath)
        currentLevel = ShowLevel.FILE
        render()
    }

    private fun render(focusDebtId: String = "NULL") {
        browser.loadHTML(buildPage(currentDebts, currentLevel!!, focusDebtId))
    }

    private fun buildPage(debts: List<DebtDto>, showLevel: ShowLevel, focusDebtId: String): String {
        val templateEngine = TemplateEngine()
        val templateResolver = ClassLoaderTemplateResolver()
        templateResolver.setTemplateMode("HTML")
        templateEngine.setTemplateResolver(templateResolver)
        val context = Context()

        addCallback(context, debts, "navigateQuery", this::navigateToCode)
        addCallback(context, debts, "showWebQuery", this::showDebtInWeb)
        addCallback(context, debts, "voteQuery", this::vote)
        addCallback(context, "showRepoDebtsQuery") { showRepoDebts() }
        addCallback(context, "showFileDebtsQuery") { showFileDebts(debts[0].bindings[0].filePath) }

        context.setVariable("debts", debts)
        context.setVariable("level", showLevel)
        context.setVariable("dark", UIUtil.isUnderDarcula())
        context.setVariable("focus", focusDebtId)
        val stringWriter = StringWriter()
        templateEngine.process("templates/debts.html", context, stringWriter)
        return stringWriter.toString()
    }

    private fun navigateToCode(debtId: String, debts: List<DebtDto>) {
        val debt = debts.find { it.id == debtId }!!
        val file = FileUtils.virtualFileByPath(project, debt.bindings[0].filePath)
        ApplicationManager.getApplication().invokeLater {
            FileEditorManager.getInstance(project).openTextEditor(
                OpenFileDescriptor(project, file, debt.bindings[0].startLine - 1, 0), true
            )
        }
    }

    private fun showDebtInWeb(debtId: String, debts: List<DebtDto>) {
        BrowserUtil.browse("${Consts.siteUrl}/debts/$debtId")
    }

    private fun vote(debtId: String, debts: List<DebtDto>) {
        val debt = debts.first { it.id == debtId }
        if (debt.voted) {
            ItHurtsClient.downVote(debt.id, {voteChangedCallback(debt)}, {})
        } else {
            ItHurtsClient.vote(debt.id, {voteChangedCallback(debt)}, {})
        }

    }

    private fun voteChangedCallback(debt: DebtDto) {
        currentDebts = currentDebts - debt
        currentDebts =
            currentDebts + debt.copy(votes = if (debt.voted) debt.votes - 1 else debt.votes + 1, voted = !debt.voted)
        currentDebts = currentDebts.sortedByDescending { it.votes }
        render(debt.id)
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
        debts: List<DebtDto>,
        callbackName: String,
        callback: (String, List<DebtDto>) -> Unit
    ) {
        addCallback(context, callbackName) { debtId ->
            callback(debtId, debts)
        }
    }
}

enum class ShowLevel {
    LINE,
    FILE,
    REPO
}