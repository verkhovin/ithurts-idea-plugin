package dev.ithurts.plugin.ide.editor

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VfsUtilCore
import dev.ithurts.plugin.common.Consts.PROJECT_REMOTE_PROPERTY_KEY
import dev.ithurts.plugin.ide.ReportDebtDialog
import dev.ithurts.plugin.ide.service.ProjectDebtsService
import org.jetbrains.annotations.Nullable

class PostDebtAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return

        ReportDebtDialog(
            project,
            relativeFilePath(editor, project),
            editor.selectionModel.selectionStartPosition!!.line,
            editor.selectionModel.selectionEndPosition!!.line
        ).showAndGet()
    }

    private fun relativeFilePath(
        editor: Editor,
        project: Project
    ): String {
        val virtualFile = FileDocumentManager.getInstance().getFile(editor.document)
            ?: throw Exception("Failed to load file")
        val projectRoot = ProjectFileIndex.SERVICE.getInstance(project).getContentRootForFile(virtualFile)
            ?: throw Exception("Failed to locate project root")
        return VfsUtilCore.getRelativePath(virtualFile, projectRoot)!!
    }

    override fun update(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        PropertiesComponent.getInstance(project)
            .getValue(PROJECT_REMOTE_PROPERTY_KEY) ?: return
        e.presentation.isEnabledAndVisible = editor.selectionModel.hasSelection()
    }
}