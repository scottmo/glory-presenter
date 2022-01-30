package com.scottscmo

import com.formdev.flatlaf.intellijthemes.FlatCarbonIJTheme
import com.scottscmo.ui.FilePicker
import com.scottscmo.ui.OutputDisplay
import com.scottscmo.ui.components.DataPathPicker
import com.scottscmo.ui.container.SlidesGenerators
import com.scottscmo.ui.container.SongFormatter
import net.miginfocom.swing.MigLayout
import javax.swing.JFrame
import javax.swing.JTabbedPane
import javax.swing.SwingUtilities

class Application() : JFrame() {

    init {
        title = "Worship Service Tool"
        defaultCloseOperation = EXIT_ON_CLOSE

        OutputDisplay.host = this
        FilePicker.host = this

        contentPane.apply {
            layout = MigLayout()
            add(DataPathPicker.create(), "wrap")
            add(JTabbedPane().apply {
                addTab("Song Formatter", SongFormatter())
                addTab("Slides Generators", SlidesGenerators())
            })
        }

        pack() // auto-resize to component, use setSize if need fixed size
    }

    // cannot put this outside of class, otherwise java -jar cannot find main
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            FlatCarbonIJTheme.setup()
            SwingUtilities.invokeLater {
                Application().apply { isVisible = true }
            }
        }
    }
}

