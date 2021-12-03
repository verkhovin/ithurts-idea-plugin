package dev.ithurts.plugin.ide.editor

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.execution.lineMarker.RunLineMarkerProvider
import com.intellij.icons.AllIcons
import com.intellij.navigation.GotoRelatedItem
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.util.NotNullFactory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.ConstantFunction

class ItHurtsLineMarkerProvider: LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
//        if (element.textRange) {
            println("here")
            return LineMarkerInfo(
                element, element.textRange, AllIcons.Actions.New, null, null, GutterIconRenderer.Alignment.CENTER
            )
//        }
        return null
    }
}