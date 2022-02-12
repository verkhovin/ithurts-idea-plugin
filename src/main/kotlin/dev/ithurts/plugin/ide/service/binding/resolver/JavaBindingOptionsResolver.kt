package dev.ithurts.plugin.ide.service.binding.resolver

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import dev.ithurts.plugin.common.FileUtils
import dev.ithurts.plugin.common.FileUtils.line
import dev.ithurts.plugin.ide.model.AdvancedBinding
import dev.ithurts.plugin.ide.model.Binding
import dev.ithurts.plugin.ide.service.binding.CodeParsingUtils
import dev.ithurts.plugin.ide.service.binding.Language
import org.slf4j.LoggerFactory

class JavaBindingOptionsResolver(private val project: Project) {
    fun getBindingOptions(editor: Editor, element: PsiElement): List<Binding> {
        val bindingOptions = mutableListOf<Binding>()
        CodeParsingUtils.iterateTreeUp(element) { elem ->
            when (elem) {
                is PsiMethod -> parseMethod(elem, editor)?.let { bindingOptions.add(it) }
                is PsiClass -> parseClass(elem, editor)?.let { bindingOptions.add(it) }
            }
        }
        return bindingOptions
    }

    private fun parseMethod(methodElem: PsiMethod, editor: Editor): Binding? {
        try {
            val methodName = methodElem.name
            val paramTypes: List<String> = methodElem.hierarchicalMethodSignature.parameterTypes.map {
                it.canonicalText
            }
            val className = PsiTreeUtil.getParentOfType(methodElem, PsiClass::class.java)?.qualifiedName
            return Binding(
                FileUtils.getRelativePath(editor),
                editor.line(methodElem.startOffset) to editor.line(methodElem.endOffset),
                AdvancedBinding(Language.JAVA, "Method", methodName, paramTypes, className)
            )
        } catch (e: Exception) {
            log.error("Failed to parse method", e)
            return null
        }
    }

    private fun parseClass(classElem: PsiClass, editor: Editor): Binding? {
        val className = classElem.qualifiedName ?: return null
        return Binding(
            FileUtils.getRelativePath(editor),
            editor.line(classElem.startOffset) to editor.line(classElem.endOffset),
            AdvancedBinding(Language.JAVA, "Class", className, emptyList(), null)
        )
    }

    companion object {
        val log = LoggerFactory.getLogger(JavaBindingOptionsResolver::class.java)
    }
}