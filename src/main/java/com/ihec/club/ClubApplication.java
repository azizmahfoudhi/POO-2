package com.ihec.club;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.swing.*;

/**
 * Point d'entrée principal de l'application.
 * Lance le serveur Spring Boot (API REST) puis l'interface graphique Swing.
 */
@SpringBootApplication
public class ClubApplication {

    public static void main(String[] args) {
        // 1. Démarrer le serveur Spring Boot (API REST sur le port 8080)
        SpringApplication.run(ClubApplication.class, args);

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
