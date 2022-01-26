package dev.ithurts.plugin.ide.service.binding

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

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
}