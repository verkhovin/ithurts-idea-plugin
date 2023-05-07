package dev.resolvt.plugin.ide.service.binding.location

import com.intellij.psi.JavaRecursiveElementWalkingVisitor
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import com.intellij.refactoring.suggested.startOffset
import dev.resolvt.plugin.ide.model.Binding

class JavaBindingSearchingElementVisitor    (
    private val bindings: List<Binding>
) : JavaRecursiveElementWalkingVisitor(), LanguageSpecificBindingLocator {
    override val bindingOffsets = mutableMapOf<String, Int>()

    override fun visitMethod(method: PsiMethod) {
        super.visitMethod(method)
        bindings
            .filter { binding -> binding.advancedBinding!!.type == "Method" }
            .forEach { binding ->
                if (method.name != binding.advancedBinding!!.name ) {
                    return@forEach
                }
                val params = method.parameterList.parameters.map { it.type.canonicalText }
                if (params != binding.advancedBinding.params) {
                    return@forEach
                }
                bindingOffsets[binding.id!!] = method.startOffset
            }
    }

    override fun visitClass(aClass: PsiClass) {
        super.visitClass(aClass)
        bindings
            .filter { it.advancedBinding!!.type == "Class" }
            .forEach {
                if(aClass.qualifiedName != it.advancedBinding!!.name) {
                    return@forEach
                }
                bindingOffsets[it.id!!] = aClass.startOffset
            }
    }
}