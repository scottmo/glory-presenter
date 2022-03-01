package com.scottscmo.ui.panels

import com.scottscmo.Config
import com.scottscmo.Config.CLIENT_INFO_KEY
import com.scottscmo.google.API_CONFIG_DIR
import com.scottscmo.google.CREDENTIALS_FILE_PATH
import com.scottscmo.ui.components.Form
import com.scottscmo.ui.components.FormInput
import com.scottscmo.util.Cryptor
import net.miginfocom.swing.MigLayout
import javax.swing.JPanel

class GSlidesPanel : JPanel() {
    init {
        layout = MigLayout()

        add(Form("Google API Credentials Importer", mapOf(
            "credentialsFilePath" to FormInput("Credentials json", "file",
                    Config.getRelativePath("$API_CONFIG_DIR/credentials.json")),
        )) {
            require(Config[CLIENT_INFO_KEY].isNotEmpty()) { "clientInfoKey is missing from config.yaml!" }

            Cryptor.encryptFile(it["credentialsFilePath"],
                Config.getRelativePath(CREDENTIALS_FILE_PATH),
                Config[CLIENT_INFO_KEY])
            "Credentials imported!"
        }.ui, "wrap")
    }
}
