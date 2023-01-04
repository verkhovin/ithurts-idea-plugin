package dev.resolvt.plugin.ide.service

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.vfs.VirtualFile
import dev.resolvt.plugin.client.ResolvtClient
import dev.resolvt.plugin.common.Consts.PROJECT_REMOTE_PROPERTY_KEY
import dev.resolvt.plugin.common.RepoUtils
import dev.resolvt.plugin.ide.service.debt.EditorDebtDisplayService
import dev.resolvt.plugin.ide.service.debt.DebtStorageService
import dev.resolvt.plugin.ide.service.debt.ResolvtGitRepositoryService

class ResolvtProjectInitiator : StartupActivity {

    override fun runActivity(project: Project) {
        val remoteUrl = RepoUtils.getRemote(project) ?: return

        val properties = PropertiesComponent.getInstance(project)
        properties.setValue(PROJECT_REMOTE_PROPERTY_KEY, remoteUrl)

        val credentialsService = service<CredentialsService>()

        if (credentialsService.hasCredentials()) {
            val client = service<ResolvtClient>()
            client.getRepository(remoteUrl) {
                project.service<ResolvtGitRepositoryService>().mainBranch = it.mainBranch
            }
            val debtStorageService = project.service<DebtStorageService>()
            client.getDebtsForRepo(
                remoteUrl,
            ) {
                debtStorageService.indexDebts(it)
                registerFileOpenedEventHandler(project)
                project.service<EditorDebtDisplayService>().renderDebtHighlighters()
                ResolvtInitiatorState.isInitialized = true
            }
        }

    }

    private fun registerFileOpenedEventHandler(project: Project) {
        if (ResolvtInitiatorState.isInitialized) {
            return
        }
        project.messageBus.connect(project)
            .subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, object : FileEditorManagerListener {
                override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
                    super.fileOpened(source, file)
                    onFileOpened(source, file)
                }
            })
    }

    private fun onFileOpened(source: FileEditorManager, file: VirtualFile) {
        val debtEditorDisplayService = source.project.service<EditorDebtDisplayService>()
        debtEditorDisplayService.renderDebtHighlighters(source.getEditors(file))
    }
}

object ResolvtInitiatorState {
    var isInitialized = false
}