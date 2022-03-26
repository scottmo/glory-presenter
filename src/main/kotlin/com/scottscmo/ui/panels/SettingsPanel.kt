package com.scottscmo.ui.panels

import com.scottscmo.Config
import com.scottscmo.ui.components.FileEditor
import com.scottscmo.ui.components.Form
import com.scottscmo.ui.components.FormInput
import com.scottscmo.util.Cryptor
import net.miginfocom.swing.MigLayout
import java.nio.file.Path
import javax.swing.JButton
import javax.swing.JPanel

class SettingsPanel : JPanel() {
    init {
        layout = MigLayout("", "[]10[]", "top")

        // config editor
        val configEditor = FileEditor(Path.of(Config.CONFIG_PATH), "", 15, 55)
        add(JPanel().apply {
            layout = MigLayout("ins 0")
            add(configEditor.ui, "wrap")
            add(JButton("Re-Apply Configurations").apply {
                addActionListener {
                    Config.load()
                }
            })
        })

        // api importer
        add(Form("Google API Credentials Importer", mapOf(
            "credentialsFilePath" to FormInput("Credentials json", "file",
                Config.getRelativePath("${Config.GOOGLE_API_DIR}/credentials.json")),
        )) {
            require(Config.get().clientInfoKey.isNotEmpty()) { "clientInfoKey is missing from config.yaml!" }

            Cryptor.encryptFile(it["credentialsFilePath"],
                Config.getRelativePath(Config.GOOGLE_API_CREDENTIALS_PATH),
                Config.get().clientInfoKey)
            "Credentials imported!"
        }.ui)
    }
}