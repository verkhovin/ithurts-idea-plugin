package dev.ithurts.plugin.ide.service.binding.resolver

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import dev.ithurts.plugin.common.FileUtils
import dev.ithurts.plugin.common.FileUtils.line
import dev.ithurts.plugin.ide.model.AdvancedBinding
import dev.ithurts.plugin.ide.model.Binding
import dev.ithurts.plugin.ide.service.binding.Language
import org.jetbrains.kotlin.idea.refactoring.fqName.fqName
import org.jetbrains.kotlin.nj2k.postProcessing.type
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class KotlinBindingOptionsResolver(private val project: Project) {
    fun getBindingOptions(editor: Editor, element: PsiElement): List<Binding> {
        val bindingOptions = mutableListOf<Binding>()
        PsiTreeUtil.getParentOfType(element, KtNamedFunction::class.java)?.let { function ->
            parseFunction(function, editor)?.let { binding -> bindingOptions.add(binding) }
        }
        PsiTreeUtil.getParentOfType(element, KtClass::class.java)?.let { ktClass ->
            parseClass(ktClass, editor)?.let { binding -> bindingOptions.add(binding) }
        }
        return bindingOptions
    }

    private fun parseFunction(function: KtNamedFunction, editor: Editor): Binding? {
        return try {
            val methodName = function.name ?: return null
            val paramTypes = resolveMethodParams(function)
            val className = PsiTreeUtil.getParentOfType(function, KtClass::class.java)?.fqName?.asString() ?: ""
            Binding(
                FileUtils.getRelativePath(editor),
                editor.line(function.funKeyword!!.startOffset) to editor.line(function.endOffset),
                AdvancedBinding(
                    Language.KOTLIN,
                    "Function",
                    methodName,
                    paramTypes,
                    className
                )
            )
        } catch (e: Exception) {
            log.error("Failed to parse function", e)
            null
        }
    }

    private fun parseClass(ktClass: KtClass, editor: Editor): Binding? {
        val className = ktClass.fqName?.asString() ?: return null
        return Binding(
            FileUtils.getRelativePath(editor),
            editor.line(ktClass.getClassKeyword()!!.startOffset) to editor.line(ktClass.getClassKeyword()!!.startOffset),
            AdvancedBinding(Language.KOTLIN, "Class", className, emptyList(), null)
        )
    }

    private fun resolveMethodParams(function: KtNamedFunction): List<String> {
        return function.valueParameters.map { param ->
            param.type()?.fqName?.asString() ?: "_"
        }.toList()
    }


    companion object {
        val log: Logger = LoggerFactory.getLogger(KotlinBindingOptionsResolver::class.java)
    }
}