package com.scottmo;

import java.awt.GraphicsEnvironment;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class Main {
    @Bean
    public WebMvcConfigurer corsMappingConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // for testing
                registry.addMapping("/**").allowedOrigins("http://localhost:3000");
            }
        };
    }

    public Main() {
        if (!GraphicsEnvironment.isHeadless()) {
            displayUI();
        }
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
    }
}
