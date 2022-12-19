package dev.resolvt.plugin.ide.service.binding

import com.intellij.lang.Language

enum class Language {
    JAVA, KOTLIN, PYTHON;

    companion object {
        fun from(language: Language): dev.resolvt.plugin.ide.service.binding.Language? {
            return when(language.id.toUpperCase()){
                "JAVA" -> JAVA
                "KOTLIN" -> KOTLIN
                "PYTHON" -> PYTHON
                else -> null
            }
        }
    }
}