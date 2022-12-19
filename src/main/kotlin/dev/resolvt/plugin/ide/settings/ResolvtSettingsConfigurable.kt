package dev.resolvt.plugin.ide.settings

import com.intellij.openapi.options.Configurable
import javax.swing.JComponent

class ResolvtSettingsConfigurable : Configurable {
    override fun createComponent(): JComponent {
        return ResolvtSettingsWindow().content
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