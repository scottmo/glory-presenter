package com.scottscmo.ui.components

import javax.swing.JComponent
import javax.swing.JSplitPane
import javax.swing.JScrollPane

/**
 * Component builder helper
 */
object C {
    // split pane
    private fun splitPane(orientation: Int, vararg components: JComponent): JComponent {
        var tail = scroll(components[0])
        for (i in 1 until components.size) {
            tail = JSplitPane(orientation, tail, scroll(components[i]))
        }
        return tail
    }

    fun resizableHBox(vararg components: JComponent): JComponent {
        return splitPane(JSplitPane.HORIZONTAL_SPLIT, *components)
    }

    fun resizableVBox(vararg components: JComponent): JComponent {
        return splitPane(JSplitPane.VERTICAL_SPLIT, *components)
    }

    // scroll pane
    fun scroll(component: JComponent): JComponent {
        return JScrollPane(component)
    }
}