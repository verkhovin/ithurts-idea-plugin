package dev.ithurts.plugin.ide.editor

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.markup.GutterIconRenderer
import dev.ithurts.plugin.ide.service.debt.DebtBrowserService
import icons.ItHurtsIcons
import javax.swing.Icon

class DebtGutterIconRenderer(
    private val debtsCount: Int,
    private val fistDebtTitle: String,
    private val filePath: String,
    private val lineNumber: Int,
    private val muted: Boolean
) : GutterIconRenderer() {
    private var clicked = false
    override fun equals(other: Any?): Boolean {
        return false
    }

    override fun hashCode(): Int {
        return 0
    }

    override fun getIcon(): Icon {
        return ItHurtsIcons.DEFAULT_ICON;
    }

    override fun getTooltipText(): String {
        return if (debtsCount > 1) "$debtsCount debts" else fistDebtTitle
    }

    override fun getAccessibleTooltipText(): String {
        return tooltipText
    }

    override fun getClickAction(): AnAction {
        return object : AnAction() {
            override fun actionPerformed(e: AnActionEvent) {
                val debtsService = e.project!!.service<DebtBrowserService>()
                debtsService.showDebts(filePath, lineNumber)
            }
        }
    }
}