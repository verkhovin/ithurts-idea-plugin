package dev.ithurts.plugin.ide.service.debt

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.markup.MarkupModel
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import dev.ithurts.plugin.common.FileUtils
import dev.ithurts.plugin.ide.editor.DebtGutterIconRenderer
import dev.ithurts.plugin.ide.model.BindingStatus
import dev.ithurts.plugin.ide.model.DebtView
import dev.ithurts.plugin.ide.model.start

class EditorDebtDisplayService(private val project: Project) {
    fun renderDebtHighlighters() {
        renderDebtHighlighters(FileEditorManager.getInstance(project).allEditors)
    }

    fun renderDebtHighlighters(fileEditors: Array<FileEditor>) {
        ApplicationManager.getApplication().executeOnPooledThread {
            fileEditors.forEach { fileEditor ->
                if (fileEditor !is TextEditor) {
                    return@forEach
                }
                val file = fileEditor.file!!
                val debtsService = project.service<DebtStorageService>()
                val relativePath = FileUtils.getRelativePath(project, file) ?: return@forEach
                val debts = debtsService.getDebts(relativePath)

                val markupModel = fileEditor.editor.markupModel
                if (debts.isEmpty()) return@forEach

                val debtGroupsByStartLine = debts.flatMap { debt ->
                    debt.bindings.map { binding ->
                        binding.actualPosition.start to (debt)
                    }
                }.groupBy({ it.first }, { it.second })

                ApplicationManager.getApplication().invokeLater {
                    removeOldHighlighters(markupModel)
                    renderNewHighlighters(debtGroupsByStartLine, markupModel, relativePath)
                }
            }
        }
    }

    private fun renderNewHighlighters(
        debtGroupsByStartLine: Map<Int, List<DebtView>>,
        markupModel: MarkupModel,
        relativePath: String,
    ) {
        debtGroupsByStartLine.forEach { (line, debts) ->
            val lineHighlighter = markupModel.addLineHighlighter(
                null, line - 1, 1
            )
            val renderAsActive = debts.flatMap { it.bindings }
                .filter { it.lines.start == line }
                .none { it.status == BindingStatus.TRACKING_LOST }
            lineHighlighter.gutterIconRenderer =
                DebtGutterIconRenderer(
                    debts.size,
                    debts[0].title,
                    relativePath,
                    line,
                    renderAsActive
                )
        }
    }

    private fun removeOldHighlighters(markupModel: MarkupModel) {
        markupModel.allHighlighters.filter { highlighter ->
            highlighter.gutterIconRenderer is DebtGutterIconRenderer
        }.forEach { markupModel.removeHighlighter(it) }
    }
}