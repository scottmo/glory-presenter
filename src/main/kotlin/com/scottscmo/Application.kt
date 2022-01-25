package com.scottscmo

import com.formdev.flatlaf.intellijthemes.FlatCarbonIJTheme
import com.scottscmo.ui.Labels
import com.scottscmo.ui.components.DataPathPicker
import com.scottscmo.ui.container.CommandRunner
import com.scottscmo.ui.container.SongFormatter
import net.miginfocom.swing.MigLayout
import java.awt.Container
import javax.swing.JFrame
import javax.swing.JTabbedPane
import javax.swing.SwingUtilities

class Application() : JFrame() {

    init {
        title = Labels.APP_NAME
        defaultCloseOperation = EXIT_ON_CLOSE

        render(contentPane)

        // auto-resize to component, use setSize if need fixed size
        pack()
    }

    private fun render(appContainer: Container) {
        val tabbedPane = JTabbedPane()
        tabbedPane.addTab(Labels.TAB_SONG_FORMATTER, SongFormatter())
        tabbedPane.addTab(Labels.TAB_COMMAND_RUNNER, CommandRunner())
        appContainer.layout = MigLayout()
        appContainer.add(DataPathPicker.create(appContainer), "wrap")
        appContainer.add(tabbedPane)
    }
}

fun main() {
    FlatCarbonIJTheme.setup()
    // Schedule a job for the event-dispatching thread:
    // creating and showing this application's GUI.
    SwingUtilities.invokeLater {
        val app = Application()
        app.isVisible = true
    }
}
