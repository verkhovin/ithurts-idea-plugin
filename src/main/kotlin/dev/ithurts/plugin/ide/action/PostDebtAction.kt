package dev.ithurts.plugin.ide.action

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import dev.ithurts.plugin.common.Consts.PROJECT_REMOTE_PROPERTY_KEY
import dev.ithurts.plugin.common.FileUtils
import dev.ithurts.plugin.common.UiUtils
import dev.ithurts.plugin.ide.service.debt.StagedDebtService

class PostDebtAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return

        val stagedDebtService = project.service<StagedDebtService>()
        stageNewDebtAndShow(stagedDebtService, project, editor)
    }

    private fun stageNewDebtAndShow(
        stagedDebtService: StagedDebtService,
        project: Project,
        editor: Editor
    ) {
        stagedDebtService.stageDebt(
            FileUtils.getRelativePath(project, editor),
            editor.offsetToLogicalPosition(editor.selectionModel.selectionStart).line + 1,
            editor.offsetToLogicalPosition(editor.selectionModel.selectionEnd - 1).line + 1,
        )

        showStagedDebt(project)
    }

    private fun showStagedDebt(
        project: Project
    ) {
        val toolWindow = UiUtils.rerenderReportDebtToolWindow(project)
        toolWindow.activate(null)
    }

    override fun update(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        PropertiesComponent.getInstance(project)
            .getValue(PROJECT_REMOTE_PROPERTY_KEY) ?: return
        e.presentation.isEnabledAndVisible = editor.selectionModel.hasSelection()
    }
}