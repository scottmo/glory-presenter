package com.scottscmo.ui.container

import net.miginfocom.swing.MigLayout
import java.awt.Color
import javax.swing.JPanel
import javax.swing.JLabel
import javax.swing.JTextArea
import javax.swing.JButton
import java.lang.Exception
import java.util.function.Consumer

class CommandRunner : JPanel() {
    init {

        // components init
        val inputLabel = JLabel("Input:")
        val commandInput = JTextArea()
        val runBtn = JButton("Run")
        val outputLabel = JLabel("Output:")
        val outputDisplay = JTextArea()

        // components config
        commandInput.rows = 3
        outputDisplay.isEditable = false
        runBtn.addActionListener { _ ->
            try {
                runCommand(commandInput.text)
            } catch (e: Exception) {
                outputDisplay.foreground = Color.RED
                outputDisplay.text = e.message
            }
        }

        // layout
        this.layout = MigLayout()
        val components = listOf(
            inputLabel,
            commandInput,
            runBtn,
            outputDisplay,
            outputLabel
        )
        components.forEach(Consumer { cmp -> this.add(cmp, "wrap") })
    }

    private fun runCommand(command: String) {}
}