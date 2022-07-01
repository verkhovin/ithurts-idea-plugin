package dev.ithurts.plugin.ide.service.debt

import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.playback.commands.ActionCommand
import dev.ithurts.plugin.client.ItHurtsClient
import dev.ithurts.plugin.client.ItHurtsError
import dev.ithurts.plugin.client.model.TechDebtReport
import dev.ithurts.plugin.common.Consts
import dev.ithurts.plugin.common.UiUtils

class DebtReportingService(private val project: Project) {
    private val propertiesComponent = PropertiesComponent.getInstance(project)
    private val stagedDebtService = project.service<StagedDebtService>()
    val client = service<ItHurtsClient>()

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

    private fun onReportFailed(error: ItHurtsError) {
        ApplicationManager.getApplication().invokeLater {
            Notifications.Bus.notify(
                Notification(
                    "",
                    "Failed to report Tech Debt to It Hurts",
                    "Error: ${error.javaClass.simpleName} ${error.message}",
                    NotificationType.ERROR
                )
            )
        }
    }

    private fun dispatchFetchDebtsAction() {
        val actionManager = ActionManager.getInstance()
        actionManager.tryToExecute(
            actionManager.getAction("ItHurtsActions.FetchDebtsAction"),
            ActionCommand.getInputEvent("ItHurtsActions.FetchDebtsAction"), null, null, true
        )
    }
}