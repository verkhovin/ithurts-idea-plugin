package dev.ithurts.plugin.ide.service

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.vcs.LocalFilePath
import com.intellij.remoteServer.util.ApplicationActionUtils
import dev.ithurts.plugin.client.ItHurtsClient
import dev.ithurts.plugin.common.Consts.PROJECT_REMOTE_PROPERTY_KEY
import git4idea.repo.GitRepositoryManager

class ItHurtsProjectInitiator : StartupActivity {

    override fun runActivity(project: Project) {
        val remoteUrl = GitRepositoryManager.getInstance(project).getRepositoryForFileQuick(
            LocalFilePath(
                project.basePath!!,
                true
            )
        )?.remotes?.first { it.name == "origin" }?.firstUrl ?: return

        val properties = PropertiesComponent.getInstance(project)
        properties.setValue(PROJECT_REMOTE_PROPERTY_KEY, remoteUrl)

        val projectDebtsService = project.service<ProjectDebtsService>()
        ItHurtsClient.getDebtsForRepo(
            remoteUrl,
            { projectDebtsService.indexDebts(it) },
            { throw Exception(it.message) }
        )
    }

}