package dev.ithurts.plugin.ide.service.binding

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

class PythonBindingOptionsResolver(private val project: Project) {
    fun getBindingOptions(element: PsiElement): List<Binding> {
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