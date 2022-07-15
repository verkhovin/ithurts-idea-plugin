package dev.ithurts.plugin.ide.toolwindow

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.layout.CellBuilder
import com.intellij.ui.layout.panel
import dev.ithurts.plugin.common.FileUtils
import dev.ithurts.plugin.common.UiUtils
import dev.ithurts.plugin.ide.service.debt.DebtReportingService
import dev.ithurts.plugin.ide.service.debt.StageMode
import dev.ithurts.plugin.ide.service.debt.StagedDebtService
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import javax.swing.JComponent


class ReportDebtToolWindow(private val project: Project) {
    private val stagedDebtService = project.service<StagedDebtService>()
    private val debtReportingService = project.service<DebtReportingService>()

    private var rootPanel: DialogPanel? = null


    fun getContent(): Content {
        val stagedDebt = stagedDebtService.stagedDebt
        rootPanel = if (stagedDebt == null || stagedDebt.bindings.isEmpty()) {
            panel {
                row {
                    label("Nothing is selected. Select some code -> right click -> \"Add Debt Binding\"")
                }
            }
        } else {
            panel {
                if (stagedDebtService.stageMode == StageMode.EDIT) {
                    row {
                        link("Cancel editing") {
                            stagedDebtService.cancelEditing()
                            UiUtils.hideReportDebtToolWindow(project)
                        }
                    }
                }
                row {
                    textField(stagedDebt::title)
                        .applyPanelOnLostFocus { rootPanel!! }
                }
                row {
                    textArea(stagedDebt::description, 40, 10).applyPanelOnLostFocus { rootPanel!! }
                }
                titledRow("Bindings:") {
                    stagedDebt.bindings.map { binding ->
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
                    button("Submit") {
                        rootPanel!!.apply()
                        debtReportingService.reportDebt()
                    }
                }

            }
        }
        return ContentFactory.SERVICE.getInstance().createContent(rootPanel!!, "", false)
    }

    private fun <T : JComponent> CellBuilder<T>.applyPanelOnLostFocus(panel: () -> DialogPanel): CellBuilder<T>  {
        this.component.addFocusListener(ApplyPanelOnFocusListener(panel))
        return this
    }
}

class ApplyPanelOnFocusListener(private val panel: () -> DialogPanel): FocusListener {
    override fun focusGained(e: FocusEvent?) {

    }

    override fun focusLost(e: FocusEvent?) {
        panel().apply()
    }
}