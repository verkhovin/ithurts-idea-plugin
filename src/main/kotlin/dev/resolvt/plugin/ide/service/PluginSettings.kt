package dev.resolvt.plugin.ide.service

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(
    name = "ResolvtPluginSettingsPersistentComponent",
    storages = [Storage("resolvt-plugin.xml")]
)
class PluginSettings: PersistentStateComponent<PluginSettings.PropertiesState> {

    var settingsState = PropertiesState()

    class PropertiesState {
        var uri = "https://resolvt.dev"
    }

    override fun getState(): PropertiesState {
        return settingsState
    }

    override fun loadState(state: PropertiesState) {
        this.settingsState = state
    }
}