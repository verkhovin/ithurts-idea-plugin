package dev.resolvt.plugin.common.swing

import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

interface SimpleDocumentListener : DocumentListener {
    fun onChange(e: DocumentEvent)

    override fun insertUpdate(e: DocumentEvent) {
        onChange(e)
    }

    override fun removeUpdate(e: DocumentEvent) {
        onChange(e)
    }

    override fun changedUpdate(e: DocumentEvent) {
        onChange(e)
    }
}