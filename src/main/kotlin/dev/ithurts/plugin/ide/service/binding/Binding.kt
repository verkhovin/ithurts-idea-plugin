package dev.ithurts.plugin.ide.service.binding

import dev.ithurts.plugin.ide.service.binding.Language.*

data class Binding(
    val language: Language,
    val type: String,
    val name: String,
    val params: List<String>,
    val parent: String?
) {
    val title: String
        get()  {
            return "$type ${formatParent()}$name${formatParams()}"
        }

    private fun formatParent(): String {
        parent ?: return ""
        return "${parent.substringAfterLast(".")}."
    }

    private fun formatParams(): String {
        if (language in listOf(JAVA, KOTLIN) && type in listOf("Method", "Function")) {
            val simplifiedParamClasses = params.map { it.substringAfterLast(".") }
            return "(${simplifiedParamClasses.joinToString(", ")})"
        }
        if (params.isEmpty()) return ""
        return "(${params.joinToString(", ")})"
    }

}