package dev.ithurts.plugin.client.model

import dev.ithurts.plugin.ide.model.BindingStatus
import dev.ithurts.plugin.ide.service.binding.Language


data class BindingDto(
    val id: String,
    val filePath: String,
    val startLine: Int,
    val endLine: Int,
    val sourceLink: SourceLink,
    val status: BindingStatus,
    val advancedBinding: AdvancedBindingDto?
) {
    override fun toString(): String {
        if (advancedBinding != null) {
            return advancedBinding.toString()
        }
        val file = filePath.substringAfterLast("/")
        val lines = "${startLine}${if (startLine == endLine) "" else "-${endLine}"}"
        return "Source code $file:$lines"
    }
}

data class AdvancedBindingDto(
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
        if (language in listOf(Language.JAVA, Language.KOTLIN) && type in listOf("Method", "Function")) {
            val simplifiedParamClasses = params.map { it.substringAfterLast(".") }
            return "(${simplifiedParamClasses.joinToString(", ")})"
        }
        if (params.isEmpty()) return ""
        return "(${params.joinToString(", ")})"
    }

    private fun formatJvmClassName(name: String) = name.substringAfterLast(".")
}
