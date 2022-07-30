package dev.ithurts.plugin.ide.service.debt

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.LocalFilePath
import git4idea.repo.GitRepositoryManager

class ItHurtsGitRepositoryService(private val project: Project) {
    private val gitRepositoryManager = GitRepositoryManager.getInstance(project)
    var mainBranch = "master"

    fun isOnMainBranch(): Boolean {
        val repository = gitRepository(project) ?: return false
        return gitRepository(project)?.currentBranch?.findTrackedBranch(repository)
            ?.name == "origin/$mainBranch" //TODO not only origin actually
    }

    private fun gitRepository(project: Project) =
        gitRepositoryManager.getRepositoryForFileQuick(
            LocalFilePath(
                project.basePath!!,
                true
            )
        )
}