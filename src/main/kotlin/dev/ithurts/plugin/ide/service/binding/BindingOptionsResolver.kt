package dev.ithurts.plugin.ide.service.binding

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement

class BindingOptionsResolver (private val project: Project) {
    fun getBindingOptions(element: PsiElement, language: Language): List<Binding> {
        return when (language) {
            Language.JAVA -> project.service<JavaBindingOptionsResolver>().getBindingOptions(element)
            Language.KOTLIN -> TODO()
            Language.PYTHON -> project.service<PythonBindingOptionsResolver>().getBindingOptions(element)
        }
    }
}