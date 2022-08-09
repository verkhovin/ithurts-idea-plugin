package dev.ithurts.plugin.ide.toolwindow

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import dev.ithurts.plugin.common.FileUtils
import dev.ithurts.plugin.common.UiUtils
import dev.ithurts.plugin.common.swing.MouseListenerWrapper
import dev.ithurts.plugin.common.swing.SimpleDocumentListener
import dev.ithurts.plugin.ide.model.Binding
import dev.ithurts.plugin.ide.model.EditableDebt
import dev.ithurts.plugin.ide.service.debt.DebtReportingService
import dev.ithurts.plugin.ide.service.debt.ItHurtsGitRepositoryService
import dev.ithurts.plugin.ide.service.debt.StageMode
import dev.ithurts.plugin.ide.service.debt.StagedDebtService
import net.miginfocom.swing.MigLayout
import java.awt.Cursor
import java.awt.event.MouseEvent
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.event.DocumentEvent
import kotlin.reflect.KMutableProperty0


class ReportDebtToolWindowOld(private val project: Project) {
    private val stagedDebtService = project.service<StagedDebtService>()
    private val debtReportingService = project.service<DebtReportingService>()
    private val mainBranch: String = project.service<ItHurtsGitRepositoryService>().mainBranch

    private val emptyRoot = JPanel(MigLayout("fill"))
    private val root = JPanel(MigLayout("fillx", "[]", "[][fill,grow][][]"))
    private val cancelEditingButton = link("Cancel editing") {
        stagedDebtService.cancelEditing()
        UiUtils.hideReportDebtToolWindow(project)
    }
    private val titleField = JBTextField()
    private val descriptionField = JBTextArea()
    private val bindingField = ComboBox<Binding>()
    private val reportButton = JButton("Submit")

    fun getContent(): Content {
        val stagedDebt = stagedDebtService.stagedDebt
        if (stagedDebt == null) {
            emptyRoot.add(
                JLabel("Nothing is selected. Select some code -> right click -> \"Add Debt Binding\""),
                "align center"
            )
            return wrapToContent(emptyRoot)
        }

        setValues(stagedDebt)

        reportButton.addActionListener { debtReportingService.reportDebt() }
        titleField.document.addDocumentListener(ValueBindingDocumentListener(stagedDebt::title) {titleField.text})
        descriptionField.document.addDocumentListener(ValueBindingDocumentListener(stagedDebt::description) {descriptionField.text})

        if (stagedDebtService.stageMode == StageMode.EDIT) {
            root.add(cancelEditingButton, "grow, span")
        }

        val descrPanel = JPanel(MigLayout("fillx", "[fill]", "[fill]"))
        descrPanel.add(descriptionField)
        root.add(titleField, "grow, span")
        root.add(descriptionField, "growx, wmin 10, span")
        bindings(stagedDebt, root)
        submitSection(stagedDebt, root)
        return wrapToContent(root)
    }

    private fun bindings(stagedDebt: EditableDebt, panel: JPanel) {
        stagedDebt.bindings.forEach { binding ->
            panel.add(link(binding.toString()) {
                val file = FileUtils.virtualFileByPath(project, binding.filePath)
                ApplicationManager.getApplication().invokeLater {
                    FileEditorManager.getInstance(project).openTextEditor(
                        OpenFileDescriptor(project, file, binding.lines.first - 1, 0), true
                    )
                }
            }, "split 2")
            panel.add(link("X") {
                stagedDebtService.removeBinding(binding)
                UiUtils.rerenderReportDebtToolWindow(project)
            }, "wrap")
        }
    }

    private fun submitSection(stagedDebt: EditableDebt, panel: JPanel) {
        if (stagedDebt.bindings.any { it.advancedBinding == null }) {
            panel.add(JBCheckBox("Fetch $mainBranch branch before submit"), "align right, split 2")
            panel.add(link("?", false) {}.also { it.toolTipText = "Debt bindings reference source code stored in the main repository branch. " +
                    "Fetching the main branch before submitting the debt lets It Hurts Integration plugin adjust " +
                    "the submission relative to the actual code." }, "align right, wrap")
        }
        panel.add(reportButton,"align right, wrap")
    }

    private fun setValues(stagedDebt: EditableDebt) {
        bindingField.removeAllItems()
        stagedDebt.bindings.forEach { binding -> bindingField.addItem(binding) }


        titleField.emptyText.text = "Title"
        descriptionField.emptyText.text = "Description"
        descriptionField.lineWrap = true

        if (stagedDebt.title.isNotBlank()) {
            titleField.text = stagedDebt.title
        }
        if (stagedDebt.description.isNotBlank()) {
            descriptionField.text = stagedDebt.description
        }
    }

    private fun wrapToContent(component: JComponent): Content {
        val contentFactory = ContentFactory.SERVICE.getInstance()
        return contentFactory.createContent(component, "", false)
    }

    companion object {
        fun link(text: String, handCursor: Boolean = true, onClick: () -> Unit): JLabel {
            val label = JLabel(text)
            label.foreground = JBColor.BLUE
            if (handCursor) {
                label.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            }
            label.addMouseListener(object: MouseListenerWrapper {
                override fun mouseClicked(e: MouseEvent?) {
                    onClick()
                }
            })
            return label
        }
    }
}

class ValueBindingDocumentListener(private val property: KMutableProperty0<String>, private val source: () -> String): SimpleDocumentListener {
    override fun onChange(e: DocumentEvent) {
        property.set(source())
    }
}