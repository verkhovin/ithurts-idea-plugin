package dev.ithurts.plugin.ide.service.binding

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import dev.ithurts.plugin.ide.service.binding.CodeParsingUtils.findClosestParent

object CodeParsingUtils {
    fun iterateTreeUp(element: PsiElement, onEach: (PsiElement) -> Unit) {
        var elem: PsiElement? = element
        while (elem != null && elem !is PsiFile) {
            onEach(elem)
            elem = elem.parent
        }
    }

    fun PsiElement.findClosestParent(parentClassName: String): PsiElement? {
        var elem: PsiElement? = this
        while (elem != null && elem !is PsiFile) {
            if(elem::class.simpleName == parentClassName) {
                return elem
            }
            elem = elem.parent
        }
        return null
    }

    fun PsiElement.findNextSibling(siblingClassName: String): PsiElement? {
        var elem: PsiElement? = this
        while (elem != null && elem !is PsiFile) {
            if(elem::class.simpleName == siblingClassName) {
                return elem
            }
            elem = elem.nextSibling
        }
        return null
    }
}