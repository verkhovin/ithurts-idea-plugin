package dev.ithurts.plugin.client.model

import dev.ithurts.plugin.ide.service.binding.Language


data class BindingDto(
    val id: String,
    val filePath: String,
    val startLine: Int,
    val endLine: Int,
    val sourceLink: SourceLink,
    val advancedBinding: AdvancedBindingDto?
) {
    fun isAdvanced(): Boolean {
        return advancedBinding != null
    }

    fun type(): String {
        return if (isAdvanced()) {
            advancedBinding!!.type
        } else {
            "Source code"
        }
    }

    private fun jvmFullName(advancedBinding: AdvancedBindingDto): String {
        val hasParent = advancedBinding.parent != null
        val hasParams = advancedBinding.params.isNotEmpty()
        return when (advancedBinding.type) {
            "Function", "Method" ->
                " ${if (hasParent) "${simpleJvmClassName(advancedBinding.parent)}#" else ""}" +
                        advancedBinding.name +
                        (if (hasParams) "(${advancedBinding.params.map(::simpleJvmClassName).joinToString()})" else "")
            else -> advancedBinding.name
        }
    }

    private fun simpleJvmClassName(className: String?): String? {
        return className?.substringAfterLast(".")
    }
}

data class AdvancedBindingDto(
    val language: Language,
    val type: String,
    val name: String,
    val params: List<String>,
    val parent: String?
)
