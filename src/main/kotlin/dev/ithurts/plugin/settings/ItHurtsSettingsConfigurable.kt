package dev.ithurts.plugin.settings

import com.intellij.openapi.options.Configurable
import com.intellij.ui.EditorTextField
import javax.swing.JComponent

class ItHurtsSettingsConfigurable: Configurable {
    override fun createComponent(): JComponent {
        return ItHurstSettingsManager().panel
    }

    override fun isModified(): Boolean {
        return false;
    }

    override fun apply() {
    }

    override fun getDisplayName(): String {
        return "The Option";
    }
}