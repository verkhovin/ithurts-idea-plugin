package dev.resolvt.plugin.ide.service.binding.location

import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.jetbrains.rd.util.first
import dev.resolvt.plugin.ide.model.Binding
import dev.resolvt.plugin.ide.service.binding.Language

class BindingLocationService {
    fun getLocation(psiFile: PsiFile, binding: Binding): Int {
        val bindingSearchingElementVisitor = getBindingSearchingElementVisitor(listOf(binding), binding.advancedBinding!!.language)
        psiFile.accept(bindingSearchingElementVisitor)
        return (bindingSearchingElementVisitor as LanguageSpecificBindingLocator).bindingOffsets.first().value
    }

    private fun getBindingSearchingElementVisitor(
        bindings: List<Binding>,
        language: Language
    ): PsiElementVisitor {
        return when(language) {
            Language.JAVA -> JavaBindingSearchingElementVisitor(bindings)
            Language.KOTLIN -> KotlinBindingSearchingElementVisitor(bindings)
            Language.PYTHON -> throw NotImplementedError()
        }
    }
}

