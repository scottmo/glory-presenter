package com.scottscmo

import com.formdev.flatlaf.FlatLightLaf
import com.scottscmo.ui.OutputDisplay
import com.scottscmo.ui.components.DataPathPicker
import com.scottscmo.ui.panels.*
import net.miginfocom.swing.MigLayout
import javax.swing.JFrame
import javax.swing.JTabbedPane
import javax.swing.SwingUtilities

class Application() : JFrame() {

    init {
        title = "Worship Service Tool"
        defaultCloseOperation = EXIT_ON_CLOSE
        isResizable = false

        contentPane.apply {
            layout = MigLayout("ins 0, wrap")
            add(DataPathPicker().ui)
            add(JTabbedPane().apply {
                addTab("Bible", BibleInfoPanel())
                addTab("Song Formatter", SongFormatterPanel())
                addTab("PPTX Generators", PPTXGeneratorsPanel())
                addTab("Google Slides", GSlidesPanel())
                addTab("Settings", SettingsPanel())
            })
        }

        pack() // auto-resize to component, use setSize if need fixed size
    }

    companion object {
        private lateinit var app: Application
        fun get(): Application {
            return app
        }
        @JvmStatic
        fun main(args: Array<String>) {
            Config.load()
            FlatLightLaf.setup()
            SwingUtilities.invokeLater {
                val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
                Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                    defaultHandler?.uncaughtException(thread, throwable)
                    // show uncaught error to user
                    OutputDisplay.error(throwable.message + "\n"
                        + throwable.stackTrace.copyOfRange(0, 10).joinToString("\n"))
                }

                app = Application().apply { isVisible = true }
            }
        }
    }
}

