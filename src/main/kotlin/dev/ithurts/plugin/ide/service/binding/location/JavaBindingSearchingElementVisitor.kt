package dev.ithurts.plugin.ide.service.binding.location

import com.intellij.psi.JavaRecursiveElementWalkingVisitor
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import com.intellij.refactoring.suggested.startOffset
import dev.ithurts.plugin.ide.model.Binding

class JavaBindingSearchingElementVisitor(
    private val bindings: List<Binding>
) : JavaRecursiveElementWalkingVisitor(), LanguageSpecificBindingLocator {
    override val bindingOffsets = mutableMapOf<String, Int>()

    override fun visitMethod(method: PsiMethod) {
        super.visitMethod(method)
        bindings
            .filter { it.advancedBinding!!.type == "Method" }
            .forEach {
                if (method.name != it.advancedBinding!!.name ) {
                    return@forEach
                }
                val params = method.parameterList.parameters.map { it.type.canonicalText }
                if (params != it.advancedBinding.params) {
                    return@forEach
                }
                bindingOffsets[it.id!!] = method.startOffset
            }
    }

    override fun visitClass(aClass: PsiClass) {
        super.visitClass(aClass)
        bindings
            .filter { it.advancedBinding!!.type == "Class" }
            .forEach {
                if(aClass.name != it.advancedBinding!!.name) {
                    return@forEach
                }
                bindingOffsets[it.id!!] = aClass.startOffset
            }
    }
}