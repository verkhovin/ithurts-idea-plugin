package dev.resolvt.plugin.ide.service.debt

import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.playback.commands.ActionCommand
import dev.resolvt.plugin.client.ResolvtClient
import dev.resolvt.plugin.client.ResolvtError
import dev.resolvt.plugin.client.model.TechDebtReport
import dev.resolvt.plugin.common.Consts
import dev.resolvt.plugin.common.UiUtils

class DebtReportingService(private val project: Project) {
    private val propertiesComponent = PropertiesComponent.getInstance(project)
    private val stagedDebtService = project.service<StagedDebtService>()
    val client = service<ResolvtClient>()

    fun reportDebt() {
        val remoteUrl = propertiesComponent.getValue(Consts.PROJECT_REMOTE_PROPERTY_KEY) ?: return
        val debt = stagedDebtService.stagedDebt ?: return
        val stageMode = stagedDebtService.stageMode
        val request = TechDebtReport.from(
            debt.title,
            debt.description,
            remoteUrl,
            debt.bindings
        )
        when (stageMode) {
            StageMode.CREATE -> {
                client.report(
                    request,
                    ::onReportSuccess,
                    ::onReportFailed
                )
            }
            StageMode.EDIT -> {
                client.editDebt(
                    debt.id!!,
                    request,
                    ::onReportSuccess,
                    ::onReportFailed
                )
            }

        }
    }

    private fun onReportSuccess() {
        ApplicationManager.getApplication().invokeLater {
            dispatchFetchDebtsAction()
            //                        propertiesComponent.setValue(Consts.SAVED_TITLE_PROPERTY_KEY, null)
            //                        propertiesComponent.setValue(Consts.SAVED_DESCRIPTION_PROPERTY_KEY, null)
            stagedDebtService.reset()
            UiUtils.getReportDebtToolWindow(project).hide()
        }
    }

    private fun onReportFailed(error: ResolvtError) {
        ApplicationManager.getApplication().invokeLater {
            Notifications.Bus.notify(
                Notification(
                    "",
                    "Failed to report Tech Debt to Resolvt",
                    "Error: ${error.javaClass.simpleName} ${error.message}",
                    NotificationType.ERROR
                )
            )
        }
    }

    private fun dispatchFetchDebtsAction() {
        val actionManager = ActionManager.getInstance()
        actionManager.tryToExecute(
            actionManager.getAction("Resolvt    Actions.FetchDebtsAction"),
            ActionCommand.getInputEvent("ResolvtActions.FetchDebtsAction"), null, null, true
        )
    }
}