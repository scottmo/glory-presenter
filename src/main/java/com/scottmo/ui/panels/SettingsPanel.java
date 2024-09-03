package com.scottmo.ui.panels;

import com.scottmo.AppLogger;
import com.scottmo.Config;
import com.scottmo.ui.components.FileEditor;
import com.scottmo.ui.components.Form;
import com.scottmo.ui.components.FormInput;
import com.scottmo.util.Cryptor;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.List;

public final class SettingsPanel extends JPanel {
    public SettingsPanel() {
        // config editor
        var configEditor = new FileEditor(Path.of(Config.CONFIG_PATH), "", 15, 55);
        var configReloadButton = new JButton("Reload Configurations");

        // api importer
        var defaultCredentialsPath = Config.getRelativePath(Config.GOOGLE_API_DIR + "/credentials.json");
        var googleApiCredentialImportForm = new Form("Google API Credentials Importer",
            List.of(
                new FormInput("credentialsFilePath", "Credentials json", "file", defaultCredentialsPath)
            ),
            form -> {
                storeGoogleAPICredentials(form.getValue("credentialsFilePath"));
                return "Credentials imported! Please restart app.";
            }
        );

        setLayout(new MigLayout("", "[]10[]", "top"));
        var configEditorContainer = new JPanel();
        configEditorContainer.setLayout(new MigLayout("ins 0"));
        configEditorContainer.add(configEditor.getUI(), "wrap");
        configEditorContainer.add(configReloadButton);
        add(new BibleInfoPanel(), "wrap");
        add(googleApiCredentialImportForm.getUI(), "wrap");
        add(configEditorContainer);

        // controls
        configReloadButton.addActionListener(e -> {
            Config.reload();
        });
    }

    private void storeGoogleAPICredentials(String credentialsFilePath) {
        // encrypt credentials
        try {
            Cryptor.encryptFile(credentialsFilePath,
                Config.getRelativePath(Config.GOOGLE_API_CREDENTIALS_PATH),
                Config.get().clientInfoKey()
            );
        } catch (GeneralSecurityException e) {
            AppLogger.showError("Failed to encrypt Google API key", e);
            return;
        } catch (IOException e) {
            AppLogger.showError("Failed to load Google API credentials", e);
            return;
        }

        // remove existing token
        var storedToken = new File(Config.getRelativePath(Config.GOOGLE_API_DIR + "/StoredCredential"));
        if (storedToken.exists()) {
            storedToken.delete();
        }
    }
}
