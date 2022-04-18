package dev.ithurts.plugin.ide.toolwindow

import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.playback.commands.ActionCommand
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.layout.panel
import dev.ithurts.plugin.client.ItHurtsClient
import dev.ithurts.plugin.client.model.TechDebtReport
import dev.ithurts.plugin.common.Consts
import dev.ithurts.plugin.common.FileUtils
import dev.ithurts.plugin.common.UiUtils
import dev.ithurts.plugin.ide.service.debt.StagedDebtService
import javax.swing.JTextArea


class ReportDebtToolWindow2(private val project: Project) {
    private val stagedDebtService = project.service<StagedDebtService>()
    private val propertiesComponent = PropertiesComponent.getInstance(project)

    private var rootPanel: DialogPanel? = null

    private var debtInfo: DebtInfo = DebtInfo("", "")

    fun getContent(): Content {
        val stagedDebt = stagedDebtService.stagedDebt
        rootPanel = if (stagedDebt == null || stagedDebt.bindingOptions.isEmpty()) {
            panel {
                row {
                    label("Nothing is selected. Select some code -> right click -> \"Report the Debt\"")
                }
            }
        } else {
            panel {
                row {
                    textField(debtInfo::title)
                }
                row {
                    textArea(debtInfo::description, 40, 10)
                }
                titledRow("Bindings") {
                    stagedDebt.bindingOptions.map { binding ->
                        row {
                            link(binding.toString()) {
                                val file = FileUtils.virtualFileByPath(project, binding.filePath)
                                ApplicationManager.getApplication().invokeLater {
                                    FileEditorManager.getInstance(project).openTextEditor(
                                        OpenFileDescriptor(project, file, binding.lines.first - 1, 0), true
                                    )
                                }
                            }
                            link("X") {
                                stagedDebtService.removeBinding(binding)
                                UiUtils.rerenderReportDebtToolWindow(project)
                            }
                        }
                    }
                }
                row {
                    button("It Hurts!") {
                        reportDebt()
                    }
                }

            }
        }
        return ContentFactory.SERVICE.getInstance().createContent(rootPanel!!, "", false)
    }

    private fun reportDebt() {
        val remoteUrl = propertiesComponent.getValue(Consts.PROJECT_REMOTE_PROPERTY_KEY) ?: return
        rootPanel!!.apply()
        stagedDebtService.stagedDebt?.let { stagedDebt ->
            service<ItHurtsClient>().report(
                TechDebtReport.from(
                    debtInfo.title,
                    debtInfo.description,
                    remoteUrl,
                    stagedDebt.bindingOptions
                ),
                {
                    ApplicationManager.getApplication().invokeLater {
                        fetchDebts()
                        propertiesComponent.setValue(Consts.SAVED_TITLE_PROPERTY_KEY, null)
                        propertiesComponent.setValue(Consts.SAVED_DESCRIPTION_PROPERTY_KEY, null)
                        stagedDebtService.reset()
                        UiUtils.getReportDebtToolWindow(project).hide()
                    }
                },
                { error ->
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
            )
        }
    }

    private fun fetchDebts() {
        val actionManager = ActionManager.getInstance()
        actionManager.tryToExecute(
            actionManager.getAction("ItHurtsActions.FetchDebtsAction"),
            ActionCommand.getInputEvent("ItHurtsActions.FetchDebtsAction"), null, null, true
        )
    }
}

class DebtInfo(
    var title: String,
    var description: String
)