package dev.ithurts.plugin.ide

import com.intellij.ui.jcef.JBCefBrowser
import java.awt.BorderLayout
import javax.swing.JPanel


class ItHurtsToolWindow(text: String) {
//    private val label: JLabel = JLabel(text)
    val content: JPanel = JPanel(BorderLayout())

    init {
        val jbCefBrowser = JBCefBrowser()
        jbCefBrowser.loadHTML("<html><body>$text</body></html>")
        content.add(jbCefBrowser.component, BorderLayout.CENTER)
    }
}