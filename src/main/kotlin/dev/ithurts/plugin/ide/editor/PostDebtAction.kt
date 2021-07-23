package dev.ithurts.plugin.ide.editor

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import dev.ithurts.plugin.common.Consts.PROJECT_REMOTE_PROPERTY_KEY
import dev.ithurts.plugin.ide.ReportDebtDialog
import dev.ithurts.plugin.ide.service.ProjectDebtsService

class PostDebtAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return;

        ReportDebtDialog(
            project,
            editor.selectionModel.selectionStartPosition!!.line,
            editor.selectionModel.selectionEndPosition!!.line
        ).showAndGet()
    }

    override fun update(e: AnActionEvent) {
        val project = e.project ?: return;
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return;
        PropertiesComponent.getInstance(project)
            .getValue(PROJECT_REMOTE_PROPERTY_KEY) ?: return
        e.presentation.isEnabledAndVisible = editor.selectionModel.hasSelection()
    }
}