package dev.ithurts.plugin.ide.service.debt

import com.intellij.ide.BrowserUtil
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefBrowserBase
import com.intellij.ui.jcef.JBCefClient.Properties.JS_QUERY_POOL_SIZE
import com.intellij.ui.jcef.JBCefJSQuery
import com.intellij.util.ui.UIUtil
import dev.ithurts.plugin.client.ItHurtsClient
import dev.ithurts.plugin.common.FileUtils
import dev.ithurts.plugin.common.UiUtils
import dev.ithurts.plugin.ide.model.DebtView
import dev.ithurts.plugin.ide.model.start
import dev.ithurts.plugin.ide.service.CredentialsService
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import java.awt.MouseInfo
import java.io.StringWriter


class DebtBrowserService(private val project: Project) {
    val browser = JBCefBrowser().also { it.setProperty(JS_QUERY_POOL_SIZE, 10000) }

    private var currentDebts = emptyList<DebtView>()
    private var currentLevel: ShowLevel? = null

    fun showDebts(filePath: String, lineNumber: Int) {
        ApplicationManager.getApplication().executeOnPooledThread {
            val debtStorageService = project.service<DebtStorageService>()
            currentDebts = debtStorageService.getDebts(filePath, lineNumber)
            currentLevel = ShowLevel.LINE

            ApplicationManager.getApplication().invokeLater {
                render()
                val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("It Hurts")!!
                toolWindow.activate(null)
            }
        }
    }

    fun showRepoDebts() {
        ApplicationManager.getApplication().executeOnPooledThread {
            val debtStorageService = project.service<DebtStorageService>()
            currentDebts = debtStorageService.getDebts().flatMap { it.value }
            currentLevel = ShowLevel.REPO
            ApplicationManager.getApplication().invokeLater {
                render()
            }
        }
    }

    fun showFileDebts(filePath: String) {
        ApplicationManager.getApplication().executeOnPooledThread {
            val debtStorageService = project.service<DebtStorageService>()
            currentDebts = debtStorageService.getDebts(filePath)
            currentLevel = ShowLevel.FILE

            ApplicationManager.getApplication().invokeLater {
                render()
            }
        }
    }

    private fun render(focusDebtId: String = "NULL") {
        browser.loadHTML(buildPage(currentDebts, currentLevel!!, focusDebtId))
    }

    private fun buildPage(debts: List<DebtView>, showLevel: ShowLevel, focusDebtId: String): String {
        val templateEngine = TemplateEngine()
        val templateResolver = ClassLoaderTemplateResolver()
        templateResolver.setTemplateMode("HTML")
        templateEngine.setTemplateResolver(templateResolver)
        val context = Context()

        addCallback(context, debts, "navigateQuery", this::navigateToCode)
        addCallback(context, debts, "showWebQuery", this::showDebtInWeb)
        addCallback(context, debts, "editDebt", this::editDebt)
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

    private fun navigateToCode(debtId: String, debts: List<DebtView>) {
        val debt = debts.find { it.id == debtId }!!
        if (debt.bindings.size == 1) {
            // if we have only one binding, navigate to it directly
            val file = FileUtils.virtualFileByPath(project, debt.bindings[0].filePath)
            ApplicationManager.getApplication().invokeLater {
                FileEditorManager.getInstance(project).openTextEditor(
                    OpenFileDescriptor(project, file, debt.bindings[0].lines.start - 1, 0), true
                )
            }
        } else {
            // otherwise, show popup with available bindings
            val instance = JBPopupFactory.getInstance()
            DataManager.getInstance().dataContextFromFocusAsync
                .onSuccess { dataContext ->
                    ApplicationManager.getApplication().invokeLater {
                        val popup = instance.createActionGroupPopup(
                            "Navigate to",
                            NavigateToBindingActionGroup(debt),
                            dataContext,
                            JBPopupFactory.ActionSelectionAid.NUMBERING,
                            true
                        )
                        popup.show(RelativePoint(MouseInfo.getPointerInfo().location))
                    }
                }
        }
    }

    private fun showDebtInWeb(debtId: String, debts: List<DebtView>) {
        BrowserUtil.browse("${service<CredentialsService>().getHost()}/debts/$debtId")
    }

    private fun editDebt(debtId: String, debts: List<DebtView>) {
        val stagedDebtService = project.service<StagedDebtService>()
        ApplicationManager.getApplication().executeOnPooledThread {
            stagedDebtService.editDebt(debtId)
            ApplicationManager.getApplication().invokeLater {
                UiUtils.rerenderReportDebtToolWindow(project)
            }
        }
    }

    private fun vote(debtId: String, debts: List<DebtView>) {
        val debt = debts.first { it.id == debtId }
        val client = service<ItHurtsClient>()
        if (debt.voted) {
            client.downVote(debt.id, { voteChangedCallback(debt) }, {})
        } else {
            client.vote(debt.id, { voteChangedCallback(debt) }, {})
        }

    }

    private fun voteChangedCallback(debt: DebtView) {
        currentDebts = currentDebts - debt
        currentDebts =
            currentDebts + debt.copy(votes = if (debt.voted) debt.votes - 1 else debt.votes + 1, voted = !debt.voted)
        currentDebts = currentDebts.sortedByDescending { it.votes }
        render(debt.id)
    }

    private fun addCallback(
        context: Context,
        callbackName: String,
        callback: (String) -> Unit,
    ) {
        val query = JBCefJSQuery.create(browser as JBCefBrowserBase)
        query.addHandler { param ->
            callback(param).let { null }
        }
        context.setVariable(callbackName, query)
    }

    private fun addCallback(
        context: Context,
        debts: List<DebtView>,
        callbackName: String,
        callback: (String, List<DebtView>) -> Unit,
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

class NavigateToBindingActionGroup(
    private val debt: DebtView,
) : ActionGroup() {
    override fun getChildren(anActionEvent: AnActionEvent?): Array<AnAction> {
        return debt.bindings.map { binding -> NavigateToBindingAction(debt.id, binding.id!!, binding.toString()) }
            .toTypedArray()
    }

    internal inner class NavigateToBindingAction(
        private val debtId: String,
        private val bindingId: String,
        text: String,
    ) : AnAction(text) {
        override fun actionPerformed(e: AnActionEvent) {
            val project = e.project ?: return
            ApplicationManager.getApplication().executeOnPooledThread {
                val actualBinding = project.service<DebtStorageService>()
                    .getDebt(debtId)
                    ?.bindings
                    ?.first { it.id == bindingId }!!
                val file = FileUtils.virtualFileByPath(project, actualBinding.filePath)
                ApplicationManager.getApplication().invokeLater {
                    FileEditorManager.getInstance(project).openTextEditor(
                        OpenFileDescriptor(project, file, actualBinding.actualPosition.start - 1, 0), true
                    )
                }
            }
        }
    }
}