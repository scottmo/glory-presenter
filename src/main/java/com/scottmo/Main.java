package com.scottmo;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.scottmo.app.App;

@SpringBootApplication
public class Main extends JFrame {

    public Main() {
        // Set up the frame
        setTitle("Glory Presenter");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTextArea textArea = new JTextArea("Server running");

        JPanel panel = new JPanel();
        panel.add(textArea);
        getContentPane().add(panel);

        pack();
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
        SpringApplication.run(Main.class);
        // App.main(args);
    }
}