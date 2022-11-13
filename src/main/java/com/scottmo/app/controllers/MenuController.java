package com.scottmo.app.controllers;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class MenuController {
    private boolean isDark;
    @FXML
    public void onToggleDarkMode(ActionEvent event) {
        if (isDark) {
            Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        } else {
            Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
        }
        isDark = !isDark;
    }
}
