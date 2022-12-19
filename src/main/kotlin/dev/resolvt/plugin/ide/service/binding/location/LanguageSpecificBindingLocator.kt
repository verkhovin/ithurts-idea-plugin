package dev.resolvt.plugin.ide.service.binding.location

interface LanguageSpecificBindingLocator {
    val bindingOffsets: MutableMap<String, Int>
}