package com.scottscmo.ui.components

import com.scottscmo.Config
import com.scottscmo.ui.FilePicker
import com.scottscmo.ui.OutputDisplay
import net.miginfocom.swing.MigLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*


class Form(title: String, inputConfigs: Map<String, FormInput>, onSubmit: (form: Form) -> String) {
    val ui = JPanel()
    private val inputs: MutableMap<String, Component> = mutableMapOf()
    private val submitBtn = JButton("Submit")

    init {
        ui.apply {
            minimumSize = Dimension(400, 0)
            layout = MigLayout("ins 0, wrap 2", "[100][100, left, fill, grow]")

            add(JLabel(title).apply { font = Font(font.name, Font.BOLD, font.size + 2) }, "span")

            for ((k, v) in inputConfigs) {
                // label
                add(JLabel(v.label))

                // input
                val input: Component
                if (listOf("file", "directory", "fileAndDirectory", "filelist").contains(v.type)) {
                    input = JTextField(v.defaultValue.ifEmpty { "select ${v.type}" }).apply {
                        addMouseListener(object : MouseAdapter() {
                            override fun mouseReleased(me: MouseEvent) {
                                FilePicker.show(v.type, v.defaultValue) { selectedPath ->
                                    text = selectedPath
                                }
                            }
                        })
                    }
                } else if (v.type == "textarea") {
                    input = JTextArea(v.height, v.width).apply {
                        font = Config.textAreaFont
                    }
                } else {
                    input = JTextField(v.defaultValue)
                }
                inputs[k] = input
                add(input)
            }

            add(submitBtn, "skip, tag apply")
        }

        submitBtn.addActionListener {
            OutputDisplay.show(onSubmit(this))
        }
    }

    operator fun get(key: String): String {
        inputs[key]?.let {
            if (it is JTextField) {
                return it.text
            } else if (it is JTextArea) {
                return it.text
            }
        }
        return ""
    }
}

data class FormInput(
    val label: String,
    val type: String = "text",
    val defaultValue: String = "",
    val height: Int = 10,
    val width: Int = 10
)
