package dev.ithurts.plugin.ide.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import dev.ithurts.plugin.common.FileUtils
import dev.ithurts.plugin.common.UiUtils
import dev.ithurts.plugin.ide.service.binding.Binding
import dev.ithurts.plugin.ide.service.binding.Language
import dev.ithurts.plugin.ide.service.binding.BindingOptionsResolver
import dev.ithurts.plugin.ide.service.debt.StagedDebtService


class PostDebtAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val elem = e.getData(CommonDataKeys.PSI_FILE)!!.findElementAt(editor.caretModel.offset)!!

        val language = Language.from(elem.language)
        val bindingOptions = if(language != null) {
            project.service<BindingOptionsResolver>().getBindingOptions(elem, language)
        } else {
            emptyList()
        }

        stageNewDebtAndShow(project, editor, bindingOptions)

    }

    private fun stageNewDebtAndShow(
        project: Project,
        editor: Editor,
        bindingOptions: List<Binding>
    ) {
        val stagedDebtService = project.service<StagedDebtService>()
        val (startLine, endLine) = if (editor.selectionModel.hasSelection()) {
            editor.offsetToLogicalPosition(editor.selectionModel.selectionStart).line + 1 to
                    editor.offsetToLogicalPosition(editor.selectionModel.selectionEnd - 1).line + 1
        } else {
            editor.caretModel.logicalPosition.line + 1 to editor.caretModel.logicalPosition.line + 1
        }
        stagedDebtService.stageDebt(
            FileUtils.getRelativePath(project, editor),
            startLine, endLine, bindingOptions
        )

        UiUtils.rerenderReportDebtToolWindow(project)



    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = true
    }
}