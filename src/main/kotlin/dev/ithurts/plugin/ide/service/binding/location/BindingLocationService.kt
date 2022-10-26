package dev.ithurts.plugin.ide.service.binding.location

import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.jetbrains.rd.util.first
import dev.ithurts.plugin.ide.model.Binding
import dev.ithurts.plugin.ide.service.binding.Language

class BindingLocationService {
//    fun getLocations(psiFile: PsiFile, bindings: List<Binding>): Map<String, Int> {
//        val bindingSearchingElementVisitor = getBindingSearchingElementVisitor(bindings)
//        psiFile.accept(bindingSearchingElementVisitor)
//        return bindingSearchingElementVisitor.bindingOffsets
//    }

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

