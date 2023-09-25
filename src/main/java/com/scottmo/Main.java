package com.scottmo;

import com.scottmo.app.App;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import javax.swing.*;

@SpringBootApplication
public class Main extends JFrame {
    @Autowired
    private ApplicationContext springContainer;

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
    }
}