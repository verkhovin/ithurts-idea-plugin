package dev.ithurts.plugin.common

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.LocalFilePath
import git4idea.repo.GitRepositoryManager

object RepoUtils {
    fun getRemote(project: Project) = GitRepositoryManager.getInstance(project).getRepositoryForFileQuick(
        LocalFilePath(
            project.basePath!!,
            true
        )
    )?.remotes?.first { it.name == "origin" }?.firstUrl ?: throw IllegalStateException("No remote found")

//    fun getMainBranch(project: Project) = GitRepositoryManager.getInstance(project).getRepositoryForFileQuick(
//        LocalFilePath(
//            project.basePath!!,
//            true
//        )
//    )?.remotes?.first { it.name == "origin" }?.
}