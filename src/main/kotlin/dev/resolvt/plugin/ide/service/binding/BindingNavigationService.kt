package dev.resolvt.plugin.ide.service.binding

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.util.PsiUtil
import dev.resolvt.plugin.common.FileUtils
import dev.resolvt.plugin.ide.model.Binding
import dev.resolvt.plugin.ide.model.start
import dev.resolvt.plugin.ide.service.binding.location.BindingLocationService

class BindingNavigationService(private val project: Project) {
    private val bindingLocationService = project.service<BindingLocationService>()

    fun navigateTo(binding: Binding) {
        val file = FileUtils.virtualFileByPath(project, binding.filePath)
        ApplicationManager.getApplication().invokeLater {
            if (binding.advancedBinding == null) {
                FileEditorManager.getInstance(project).openTextEditor(
                    OpenFileDescriptor(project, file, binding.lines.start - 1, 0), true
                )
            } else {
                val location = bindingLocationService.getLocation(PsiUtil.getPsiFile(project, file), binding)
                FileEditorManager.getInstance(project).openTextEditor(
                    OpenFileDescriptor(project, file, location), true
                )
            }
        }
    }
}