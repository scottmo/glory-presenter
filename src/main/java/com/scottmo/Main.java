package com.scottmo;

import java.awt.Desktop;
import java.awt.EventQueue;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class Main extends JFrame {
    @Bean
    public WebMvcConfigurer corsMappingConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                // for testing
                registry.addMapping("/**").allowedOrigins("http://localhost:3000");
            }
        };
    }

    public Main() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Glory Presenter");
        this.setSize(400, 300);

        JButton openAppButton = new JButton("Launch UI");
        openAppButton.addActionListener(e -> {
            openWebpage("http://localhost:8080/#/songs");
        });
        JTextArea textArea = new JTextArea("Status: running");

        JPanel panel = new JPanel();
        panel.add(textArea);
        panel.add(openAppButton);
        this.getContentPane().add(panel);
    }

    private boolean openWebpage(URI uri) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(uri);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private boolean openWebpage(String url) {
        try {
            return openWebpage(new URL(url).toURI());
        } catch (MalformedURLException | URISyntaxException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = new SpringApplicationBuilder(Main.class)
            .headless(false).run(args);

        EventQueue.invokeLater(() -> {
            Main app = ctx.getBean(Main.class);
            app.setVisible(true);
        });
    }
}
