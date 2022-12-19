package dev.resolvt.plugin.ide.service.debt

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.markup.MarkupModel
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.util.PsiUtil
import dev.resolvt.plugin.common.FileUtils
import dev.resolvt.plugin.ide.editor.DebtGutterIconRenderer
import dev.resolvt.plugin.ide.model.Binding
import dev.resolvt.plugin.ide.model.BindingStatus
import dev.resolvt.plugin.ide.model.DebtView
import dev.resolvt.plugin.ide.model.start
import dev.resolvt.plugin.ide.service.binding.location.BindingLocationService

class EditorDebtDisplayService(private val project: Project) {
    private val bindingLocationService: BindingLocationService = project.service()

    fun renderDebtHighlighters() {
        renderDebtHighlighters(FileEditorManager.getInstance(project).allEditors)
    }

    fun renderDebtHighlighters(fileEditors: Array<FileEditor>) {
        fileEditors.forEach { fileEditor ->
            if (fileEditor !is TextEditor) {
                return@forEach
            }
            val file = fileEditor.file!!
            val debtsService = project.service<DebtStorageService>()
            val relativePath = FileUtils.getRelativePath(project, file) ?: return@forEach
            val debts = debtsService.getDebts(relativePath)
            if (debts.isEmpty()) return@forEach

            ApplicationManager.getApplication().invokeLater {
                val markupModel = fileEditor.editor.markupModel

                val debtGroupsByStartLine = debts.flatMap { debt ->
                    debt.bindings.map { binding -> getLine(binding, file, markupModel.document) to (debt) }
                }.groupBy({ it.first }, { it.second })

                removeOldHighlighters(markupModel)
                renderNewHighlighters(debtGroupsByStartLine, markupModel, relativePath)
            }
        }
    }

    private fun getLine(binding: Binding, file: VirtualFile, document: Document): Int {
        if (binding.advancedBinding == null) {
            return binding.lines.start
        }
        val psiFile = PsiUtil.getPsiFile(project, file)
        val offset = bindingLocationService.getLocation(psiFile, binding)
        return document.getLineNumber(offset) + 1
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