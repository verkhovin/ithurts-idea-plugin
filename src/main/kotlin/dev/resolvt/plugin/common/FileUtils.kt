package dev.resolvt.plugin.common

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.LocalFilePath
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import git4idea.repo.GitRepositoryManager

object FileUtils {
    fun getRelativePath(
        editor: Editor
    ): String {
        val virtualFile = FileDocumentManager.getInstance().getFile(editor.document)
            ?: throw Exception("Failed to load file")
        return getRelativePath(editor.project!!, virtualFile)!!
    }

    fun getRelativePath(
        project: Project,
        file: VirtualFile
    ): String? {
        val projectRoot = getProjectRoot(project)
        return VfsUtilCore.getRelativePath(file, projectRoot)
    }

    fun getProjectRoot(project: Project) =
        GitRepositoryManager.getInstance(project).getRepositoryForFileQuick(
            LocalFilePath(
                project.basePath!!,
                true
            )
        )?.root ?: throw Exception("Failed to locate project root")

    fun virtualFileByPath(project: Project, path: String) = getProjectRoot(project).findFileByRelativePath(path)
        ?: throw Exception("Failed to locate file")


    fun Editor.line(offset: Int): Int {
        return this.offsetToLogicalPosition(offset).line + 1
    }

}