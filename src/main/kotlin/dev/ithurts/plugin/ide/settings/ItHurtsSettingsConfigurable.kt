package dev.ithurts.plugin.ide.settings

import com.intellij.openapi.options.Configurable
import javax.swing.JComponent

class ItHurtsSettingsConfigurable : Configurable {
    override fun createComponent(): JComponent {
        return ItHurtsSettingsWindow().content
    }

    override fun isModified(): Boolean {
        return false
    }

    override fun apply() {
    }

    override fun getDisplayName(): String {
        return "The Option"
    }
}