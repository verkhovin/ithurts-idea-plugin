package dev.ithurts.plugin.ide

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.ui.JBColor
import dev.ithurts.plugin.client.ItHurtsClient
import dev.ithurts.plugin.common.Consts
import dev.ithurts.plugin.common.Consts.SAVED_DESCRIPTION_PROPERTY_KEY
import dev.ithurts.plugin.common.Consts.SAVED_TITLE_PROPERTY_KEY
import dev.ithurts.plugin.model.TechDebtReport
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Toolkit
import javax.swing.*
import kotlin.math.roundToInt


class ReportDebtDialog(private val project: Project, val startLine: Int, val endLine: Int): DialogWrapper(true) {
    private val titleField = JTextField()
    private val descriptionField = JTextArea()

    private val propertiesComponent = PropertiesComponent.getInstance(project)

    override fun createCenterPanel(): JComponent {
        val dialogPanel = JPanel(BorderLayout())
        dialogPanel.add(titleField, BorderLayout.PAGE_START)
        dialogPanel.add(descriptionField, BorderLayout.CENTER)
        val screenSize = Toolkit.getDefaultToolkit().screenSize
        dialogPanel.preferredSize = Dimension((screenSize.width * 0.3).roundToInt(), (screenSize.height * 0.3).roundToInt())

        titleField.text = propertiesComponent.getValue(SAVED_TITLE_PROPERTY_KEY) ?: ""
        descriptionField.text = propertiesComponent.getValue(SAVED_DESCRIPTION_PROPERTY_KEY) ?: ""

        if (!descriptionField.text.endsWith("\n")) {
            descriptionField.append("\n")
            descriptionField.caretPosition = 0
        }

        return dialogPanel
    }

    init {
        title = "Report to It Hurts"
        setOKButtonText("It Hurts!")

        descriptionField.border = BorderFactory.createLineBorder(JBColor.border())
        descriptionField.font = titleField.font

        init()
    }

    override fun doCancelAction() {
        propertiesComponent.setValue(SAVED_TITLE_PROPERTY_KEY, titleField.text)
        propertiesComponent.setValue(SAVED_DESCRIPTION_PROPERTY_KEY, descriptionField.text)
        super.doCancelAction()
    }

    override fun doOKAction() {
        val remoteUrl = PropertiesComponent.getInstance(project)
            .getValue(Consts.PROJECT_REMOTE_PROPERTY_KEY) ?: return

        ItHurtsClient.report(
            TechDebtReport(
                titleField.text,
                descriptionField.text,
                remoteUrl,
                startLine,
                endLine
            ),
            { super.doOKAction() },
            { error -> Messages.showErrorDialog(
                project, "Something went wrong. Please, try again later. Reason: ${error.message}",
                "Debt Report Failed") }
        )
    }
}