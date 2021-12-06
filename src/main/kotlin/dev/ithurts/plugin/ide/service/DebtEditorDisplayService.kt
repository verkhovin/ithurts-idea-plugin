package dev.ithurts.plugin.ide.service

import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import dev.ithurts.plugin.common.FileUtils
import dev.ithurts.plugin.ide.editor.DebtGutterIconRenderer

class DebtEditorDisplayService(private val project: Project) {
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
            val relativePath = FileUtils.getRelativePath(project, file)
            val debts = debtsService.getDebts(relativePath)
            if (debts.isEmpty()) return@forEach
            val debtGroupsByStartLine = debts.groupBy { it.startLine }

            debtGroupsByStartLine.forEach { (line, debts) ->
                val addLineHighlighter = fileEditor.editor.markupModel.addLineHighlighter(
                    null, line, 1
                )
                addLineHighlighter.gutterIconRenderer =
                    DebtGutterIconRenderer(debts.size, debts[0].title, relativePath, line)
            }

        }
    }
}