package dev.ithurts.plugin.ide.service.binding

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import dev.ithurts.plugin.ide.service.binding.CodeParsingUtils.findClosestParent
import org.slf4j.LoggerFactory

class JavaBindingOptionsResolver(private val project: Project) {
    fun getBindingOptions(element: PsiElement): List<Binding> {
        val bindingOptions = mutableListOf<Binding>()
        CodeParsingUtils.iterateTreeUp(element) { elem ->
            when (elem::class.simpleName) {
                "PsiMethodImpl" -> parseMethod(elem)?.let { bindingOptions.add(it) }
            }
        }
        return bindingOptions
    }

    private fun parseMethod(elem: PsiElement): Binding? {
        try {
            val methodName = elem.children.first { it::class.simpleName == "PsiIdentifierImpl" }.text
            val paramTypes = resolveMethodParams(elem)
            val className = resolvePsiClassName(elem.findClosestParent("PsiClassImpl")!!)
            return Binding(Language.JAVA, "Method", methodName, paramTypes, className)
        } catch (e: Exception) {
            log.error("Failed to parse method", e)
            return null
        }
    }

    private fun resolveMethodParams(elem: PsiElement): List<String> {
         val psiClasses = elem.children.first { it::class.simpleName == "PsiParameterListImpl" }
            .children.filter { it::class.simpleName == "PsiParameterImpl" }
            .map { param ->
                param.children.first { it::class.simpleName == "PsiTypeElementImpl" }
                    .children.first { typeElem -> typeElem::class.simpleName == "PsiJavaCodeReferenceElementImpl" }
                    .reference!!.resolve()!! //TODO handle nulls
            }

       return psiClasses.map { psiClass ->
           resolvePsiClassName(psiClass)
        }
    }

    private fun resolvePsiClassName(psiClass: PsiElement): String =
        ReflectionUtils.invokeMethod(psiClass, "getQualifiedName")

    companion object {
        val log = LoggerFactory.getLogger(JavaBindingOptionsResolver::class.java)
    }
}