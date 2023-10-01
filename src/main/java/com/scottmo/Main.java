package com.scottmo;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.scottmo.app.App;

@SpringBootApplication
public class Main {

    public Main() {
        // displayUI();
    }

    private void displayUI() {
        JFrame sysUI = new JFrame("Glory Presenter");

        sysUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTextArea textArea = new JTextArea("Server running");

        JPanel panel = new JPanel();
        panel.add(textArea);
        sysUI.getContentPane().add(panel);

        sysUI.pack();
        sysUI.setVisible(true);
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
        // App.main(args);
    }
}
