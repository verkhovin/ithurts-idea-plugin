package dev.ithurts.plugin.ide.service.binding.resolver

import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import dev.ithurts.plugin.common.FileUtils
import dev.ithurts.plugin.common.FileUtils.line
import dev.ithurts.plugin.ide.model.Binding
import dev.ithurts.plugin.ide.service.binding.Language
import dev.ithurts.plugin.ide.service.debt.ItHurtsGitRepositoryService

class BindingOptionsResolver (private val project: Project) {
    private val repositoryService = project.service<ItHurtsGitRepositoryService>()

    fun getBindingOptions(editor: Editor, element: PsiElement, language: Language?): List<Binding> {
        val advancedBindings: List<Binding> = when (language) {
            Language.JAVA -> project.service<JavaBindingOptionsResolver>().getBindingOptions(editor, element)
            Language.KOTLIN -> project.service<KotlinBindingOptionsResolver>().getBindingOptions(editor, element)
            Language.PYTHON -> emptyList() //project.service<PythonBindingOptionsResolver>().getBindingOptions(element)
            else -> emptyList()
        }
        if (repositoryService.isOnMainBranch()) {
            val basicBinding = basicBinding(editor)
            return advancedBindings + basicBinding
        }
        return advancedBindings
    }

    private fun basicBinding(editor: Editor) =
        Binding(
            FileUtils.getRelativePath(editor),
            if (editor.selectionModel.hasSelection()) {
                editor.line(editor.selectionModel.selectionStart) to
                        editor.line(editor.selectionModel.selectionEnd - 1)
            } else {
                editor.caretModel.logicalPosition.line + 1 to editor.caretModel.logicalPosition.line + 1
            },
        )
}