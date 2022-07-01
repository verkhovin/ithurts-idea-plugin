package dev.ithurts.plugin.ide.model

class EditableDebt(
    val id: String? = null,
    var title: String = "",
    var description: String = "",
    var bindings: MutableList<Binding>
)