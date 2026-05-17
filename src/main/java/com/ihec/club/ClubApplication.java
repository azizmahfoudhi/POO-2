package com.ihec.club;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.builder.SpringApplicationBuilder;
import javax.swing.*;

/**
 * Point d'entrée principal de l'application.
 * Lance le serveur Spring Boot (API REST) puis l'interface graphique Swing.
 */
@SpringBootApplication
public class ClubApplication {

    public static void main(String[] args) {
        // 1. Démarrer le serveur Spring Boot avec le mode headless désactivé (obligatoire pour Swing)
        new SpringApplicationBuilder(ClubApplication.class)
            .headless(false)
            .run(args);

        // 2. Lancer l'interface graphique Swing
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new com.ihec.club.gui.MainGUI();
        });
    }
}
