package dev.ithurts.plugin.ide.service.binding.location

interface LanguageSpecificBindingLocator {
    val bindingOffsets: MutableMap<String, Int>
}