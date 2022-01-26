package com.scottscmo.ui.components

import com.scottscmo.ui.OutputDisplay
import net.miginfocom.swing.MigLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField


class Form(title: String, inputConfigs: Map<String, FormInput>, onSubmit: (form: Form) -> String) {
    val ui = JPanel()
    private val inputs: MutableMap<String, Component> = mutableMapOf()
    private val submitBtn = JButton("Submit")

    init {
        ui.apply {
            minimumSize = Dimension(300, 0)
            layout = MigLayout("gap 5", "[100]5[100, left, fill, grow]", "[][]20[]")

            add(JLabel(title).apply { font = Font(font.name, Font.BOLD, font.size + 2) }, "span")

            for ((k, v) in inputConfigs) {
                if (v.type == "text") {
                    val input = JTextField().apply { text = v.defaultValue }
                    inputs[k] = input
                    add(JLabel(v.label))
                    add(input, "wrap")
                }
            }

            add(submitBtn, "skip, tag apply")
        }

        submitBtn.addActionListener {
            try {
                OutputDisplay.show(onSubmit(this))
            } catch (e: Exception) {
                OutputDisplay.error(e.message)
            }
        }
    }

    operator fun get(key: String): String {
        inputs[key]?.let {
            if (it is JTextField) {
                return it.text
            }
        }
        return ""
    }
}

data class FormInput(
    val label: String,
    val type: String = "text",
    val defaultValue: String = "") {}
