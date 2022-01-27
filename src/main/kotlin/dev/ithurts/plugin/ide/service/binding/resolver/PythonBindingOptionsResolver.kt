package dev.ithurts.plugin.ide.service.binding.resolver

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import dev.ithurts.plugin.ide.model.AdvancedBinding

class PythonBindingOptionsResolver(private val project: Project) {
    fun getBindingOptions(element: PsiElement): List<AdvancedBinding> {
        var elem = element
        while (elem !is PsiFile) {
            when(elem::class.simpleName) {
                "PyFunctionImpl" -> throw IllegalArgumentException("Found Python function!")
            }
            elem = elem.parent
        }
        return emptyList()
    }
}