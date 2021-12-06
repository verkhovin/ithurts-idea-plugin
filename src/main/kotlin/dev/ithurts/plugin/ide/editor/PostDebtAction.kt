package dev.ithurts.plugin.ide.editor

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import dev.ithurts.plugin.common.Consts.PROJECT_REMOTE_PROPERTY_KEY
import dev.ithurts.plugin.common.FileUtils
import dev.ithurts.plugin.ide.ReportDebtDialog

class PostDebtAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return

        ReportDebtDialog(
            project,
            editor,
            FileUtils.getRelativePath(project, editor),
            editor.offsetToLogicalPosition(editor.selectionModel.selectionStart).line,
            editor.offsetToLogicalPosition(editor.selectionModel.selectionEnd).line,
        ).showAndGet()
    }

    override fun update(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        PropertiesComponent.getInstance(project)
            .getValue(PROJECT_REMOTE_PROPERTY_KEY) ?: return
        e.presentation.isEnabledAndVisible = editor.selectionModel.hasSelection()
    }
}