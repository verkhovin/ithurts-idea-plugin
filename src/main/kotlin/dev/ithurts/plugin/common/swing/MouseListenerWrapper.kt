package dev.ithurts.plugin.common.swing

import java.awt.event.MouseEvent
import java.awt.event.MouseListener

interface MouseListenerWrapper: MouseListener {
    override fun mouseClicked(e: MouseEvent?) {
        //do nothing by default
    }

    override fun mousePressed(e: MouseEvent?) {
        //do nothing by default
    }

    override fun mouseReleased(e: MouseEvent?) {
        //do nothing by default
    }

    override fun mouseEntered(e: MouseEvent?) {
        //do nothing by default
    }

    override fun mouseExited(e: MouseEvent?) {
        //do nothing by default
    }
}