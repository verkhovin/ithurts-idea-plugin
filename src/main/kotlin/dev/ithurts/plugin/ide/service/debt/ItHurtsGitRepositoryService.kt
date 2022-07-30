package dev.ithurts.plugin.ide.service.debt

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.LocalFilePath
import git4idea.repo.GitRepositoryManager

class ItHurtsGitRepositoryService(private val project: Project) {
    private val gitRepositoryManager = GitRepositoryManager.getInstance(project)
    var mainBranch = "master"
    val repository
        get() = gitRepositoryManager.getRepositoryForFileQuick(
            LocalFilePath(
                project.basePath!!,
                true
            )
        )!!
}