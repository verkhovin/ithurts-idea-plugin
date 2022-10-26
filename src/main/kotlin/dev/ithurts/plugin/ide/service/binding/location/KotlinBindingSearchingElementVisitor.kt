package dev.ithurts.plugin.ide.service.binding.location

import com.intellij.refactoring.suggested.startOffset
import dev.ithurts.plugin.ide.model.Binding
import org.jetbrains.kotlin.idea.refactoring.fqName.fqName
import org.jetbrains.kotlin.idea.structuralsearch.visitor.KotlinRecursiveElementWalkingVisitor
import org.jetbrains.kotlin.nj2k.postProcessing.type
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtNamedFunction

class KotlinBindingSearchingElementVisitor(
    private val bindings: List<Binding>
) : KotlinRecursiveElementWalkingVisitor(), LanguageSpecificBindingLocator {
    override val bindingOffsets = mutableMapOf<String, Int>()

    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)
        bindings
            .filter { it.advancedBinding!!.type == "Function" }
            .forEach {
                if (function.name != it.advancedBinding!!.name ) {
                    return@forEach
                }
                val params = function.valueParameters.map { it.type()!!.fqName!!.asString() }
                if (params != it.advancedBinding.params) {
                    return@forEach
                }
                bindingOffsets[it.id!!] = function.startOffset
            }
    }

    override fun visitClass(klass: KtClass) {
        super.visitClass(klass)
        bindings
            .filter { it.advancedBinding!!.type == "Class" }
            .forEach {
                if(klass.name != it.advancedBinding!!.name) {
                    return@forEach
                }
                bindingOffsets[it.id!!] = klass.startOffset
            }
    }
}