package com.scottscmo.ui.components

import javax.swing.JComponent
import javax.swing.JSplitPane
import javax.swing.JScrollPane

/**
 * Component builder helper
 */
object C {
    private fun splitPane(orientation: Int, vararg components: JComponent): JComponent {
        var tail: JComponent = JScrollPane(components[0])
        for (i in 1 until components.size) {
            tail = JSplitPane(orientation, tail, JScrollPane(components[i]))
        }
        return tail
    }

    fun resizableHBox(vararg components: JComponent): JComponent {
        return splitPane(JSplitPane.HORIZONTAL_SPLIT, *components)
    }

    fun resizableVBox(vararg components: JComponent): JComponent {
        return splitPane(JSplitPane.VERTICAL_SPLIT, *components)
    }
}