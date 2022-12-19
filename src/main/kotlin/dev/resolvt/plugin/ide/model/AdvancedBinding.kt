package dev.resolvt.plugin.ide.model

import dev.resolvt.plugin.ide.service.binding.Language
import dev.resolvt.plugin.ide.service.binding.Language.JAVA
import dev.resolvt.plugin.ide.service.binding.Language.KOTLIN

class AdvancedBinding(
    val language: Language,
    val type: String,
    val name: String,
    val params: List<String>,
    val parent: String?
) {
    override fun toString(): String = "$type ${formatParent()}${formatName()}${formatParams()}"

    private fun formatName(): String {
        return if (type.toUpperCase() == "CLASS") {
            formatJvmClassName(name)
        } else {
            name
        }
    }

    private fun formatParent(): String {
        parent ?: return ""
        return "${formatJvmClassName(parent)}#"
    }

    private fun formatParams(): String {
        if (language in listOf(JAVA, KOTLIN) && type in listOf("Method", "Function")) {
            val simplifiedParamClasses = params.map { it.substringAfterLast(".") }
            return "(${simplifiedParamClasses.joinToString(", ")})"
        }
        if (params.isEmpty()) return ""
        return "(${params.joinToString(", ")})"
    }

    private fun formatJvmClassName(name: String) = name.substringAfterLast(".")
}

