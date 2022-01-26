package dev.ithurts.plugin.ide.toolwindow

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.playback.commands.ActionCommand
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import dev.ithurts.plugin.client.ItHurtsClient
import dev.ithurts.plugin.common.Consts
import dev.ithurts.plugin.common.FileUtils
import dev.ithurts.plugin.common.UiUtils.getReportDebtToolWindow
import dev.ithurts.plugin.common.swing.MouseListenerWrapper
import dev.ithurts.plugin.common.swing.SimpleDocumentListener
import dev.ithurts.plugin.ide.service.debt.StagedDebt
import dev.ithurts.plugin.ide.service.debt.StagedDebtService
import dev.ithurts.plugin.model.TechDebtReport
import net.miginfocom.swing.MigLayout
import java.awt.Cursor
import java.awt.event.MouseEvent
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.event.DocumentEvent


class ReportDebtToolWindow(private val project: Project) {
    private val stagedDebtService = project.service<StagedDebtService>()
    private val propertiesComponent = PropertiesComponent.getInstance(project)

    private val emptyRoot = JPanel(MigLayout("fill"))
    private val root = JPanel(MigLayout("fillx", "[]", "[][fill,grow][][]"))
    private val titleField = JBTextField()
    private val descriptionField = JBTextArea()
    private val bindingField = ComboBox<String>()
    private val codeReferenceLabel = JLabel()
    private val reportButton = JButton("It hurts!")

    private val fieldsChangedListener = object : SimpleDocumentListener {
        override fun onChange(e: DocumentEvent) {
            stagedDebtService.saveTitleAndDescription(titleField.text, descriptionField.text)
        }
    }

    fun getContent(): Content {
        val stagedDebt = stagedDebtService.stagedDebt
        if (stagedDebt == null) {
            emptyRoot.add(
                JLabel("Nothing is selected. Select some code -> right click -> \"Report the Debt\""),
                "align center"
            )
            return wrapToContent(emptyRoot)
        }

        setValues(stagedDebt)
        addListeners(stagedDebt)

        root.add(titleField, "grow, span")
        root.add(descriptionField, "grow, span")
        root.add(bindingField, "grow, span")
        root.add(codeReferenceLabel, "align right, wrap")
        root.add(reportButton, "align right")

        return wrapToContent(root)
    }

    private fun setValues(stagedDebt: StagedDebt) {
        val selectedCodeLinkText =
            "${stagedDebt.filePath.substringAfterLast("/")}:${stagedDebt.startLine}" +
                    if (stagedDebt.startLine != stagedDebt.endLine) "-${stagedDebt.endLine}" else ""

        bindingField.removeAllItems()
        stagedDebt.bindingOptions.forEach { binding -> bindingField.addItem(binding.title) }
        bindingField.addItem("Source code $selectedCodeLinkText")

        codeReferenceLabel.text = selectedCodeLinkText
        codeReferenceLabel.foreground = JBColor.BLUE
        codeReferenceLabel.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)


        titleField.emptyText.text = "Title: what's the problem?"
        descriptionField.emptyText.text = "Why it is bad? How it should be? Some options to fix it (optional)"

        if (!stagedDebt.title.isNullOrBlank()) {
            titleField.text = stagedDebt.title
        }
        if (!stagedDebt.description.isNullOrBlank()) {
            descriptionField.text = stagedDebt.description
        }
    }

    private fun addListeners(stagedDebt: StagedDebt) {
        reportButton.addActionListener { reportDebt(stagedDebt) }
        titleField.document.addDocumentListener(fieldsChangedListener)
        descriptionField.document.addDocumentListener(fieldsChangedListener)
        codeReferenceLabel.addMouseListener(object : MouseListenerWrapper {
            override fun mouseClicked(e: MouseEvent?) {
                val file = FileUtils.virtualFileByPath(project, stagedDebt.filePath)
                ApplicationManager.getApplication().invokeLater {
                    FileEditorManager.getInstance(project).openTextEditor(
                        OpenFileDescriptor(project, file, stagedDebt.startLine - 1, 0), true
                    )
                }
            }
        })
    }

    private fun wrapToContent(component: JComponent): Content {
        val contentFactory = ContentFactory.SERVICE.getInstance()
        return contentFactory.createContent(component, "", false)
    }

    private fun reportDebt(stagedDebt: StagedDebt) {
        val remoteUrl = propertiesComponent.getValue(Consts.PROJECT_REMOTE_PROPERTY_KEY) ?: return

        val binding = if (bindingField.itemCount == 1) { //default only
            null
        } else {
            stagedDebtService.stagedDebt!!.bindingOptions[bindingField.selectedIndex]
        }

        ItHurtsClient.report(
            TechDebtReport(
                titleField.text,
                descriptionField.text,
                remoteUrl,
                stagedDebt.filePath,
                stagedDebt.startLine,
                stagedDebt.endLine,
                binding
            ),
            {
                ApplicationManager.getApplication().invokeLater {
                    fetchDebts()
                    propertiesComponent.setValue(Consts.SAVED_TITLE_PROPERTY_KEY, null)
                    propertiesComponent.setValue(Consts.SAVED_DESCRIPTION_PROPERTY_KEY, null)
                    stagedDebtService.reset()
                    getReportDebtToolWindow(project).hide()
                }
            },
            { error ->
                Messages.showErrorDialog(
                    project, "Something went wrong. Please, try again later. Reason: ${error.message}",
                    "Debt Report Failed"
                )
            }
        )
    }

    private fun fetchDebts() {
        val actionManager = ActionManager.getInstance()
        actionManager.tryToExecute(
            actionManager.getAction("ItHurtsActions.FetchDebtsAction"),
            ActionCommand.getInputEvent("ItHurtsActions.FetchDebtsAction"), null, null, true
        )
    }
}