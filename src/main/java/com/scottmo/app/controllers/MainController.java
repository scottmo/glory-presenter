package com.scottmo.app.controllers;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MainController {

    @FXML
    private Label statusLabel;
    // menu
    private boolean isDark;
    @FXML
    private void onToggleDarkMode(ActionEvent actionEvent) {
        if (isDark) {
            Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        } else {
            Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
        }
        isDark = !isDark;
    }
}
