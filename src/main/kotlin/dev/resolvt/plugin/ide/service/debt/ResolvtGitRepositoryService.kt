package dev.resolvt.plugin.ide.service.debt

import com.intellij.openapi.project.Project

class ResolvtGitRepositoryService(private val project: Project) {
    var mainBranch = "master"
}