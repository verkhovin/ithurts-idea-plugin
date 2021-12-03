package dev.ithurts.plugin.ide.editor

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.markup.GutterIconRenderer
import javax.swing.Icon

class DebtGutterIconRenderer(private val text: String): GutterIconRenderer() {
    private var clicked = false;
    override fun equals(other: Any?): Boolean {
        return false
    }

    override fun hashCode(): Int {
        return 0;
    }

    override fun getIcon(): Icon {
        return if (clicked) AllIcons.General.ExclMark else AllIcons.Actions.New
    }

    override fun getTooltipText(): String {
        return "$text / Click if it hurts you too";
    }

    override fun getAccessibleTooltipText(): String? {
        return tooltipText
    }

    override fun getClickAction(): AnAction {
        return object : AnAction() {
            override fun actionPerformed(e: AnActionEvent) {
                clicked = true;
            }
        }
    }
}