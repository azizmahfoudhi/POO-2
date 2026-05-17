package com.ihec.club.gui;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.List;

/**
 * Fenêtre principale de l'application Swing.
 * Communique avec le backend Spring Boot via l'ApiClient REST.
 */
public class MainGUI extends JFrame {

    private JPanel mainPanel;
    private CardLayout cardLayout;
    private Map<String, Object> utilisateurCourant = null;

    public MainGUI() {
        super("Système de Gestion des Clubs de l'IHEC");
        initialiserGUI();
    }

    private void initialiserGUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        JPanel loginPanel = creerPanneauConnexion();
        mainPanel.add(loginPanel, "LOGIN");

        add(mainPanel);
        setVisible(true);
    }

    private JPanel creerPanneauConnexion() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 248, 255));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Titre
        JLabel titre = new JLabel("SYSTÈME DE GESTION DES CLUBS DE L'IHEC");
        titre.setFont(new Font("Arial", Font.BOLD, 20));
        titre.setForeground(new Color(25, 25, 112));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(titre, gbc);

        JLabel sousTitre = new JLabel("Gestion des Événements — Spring Boot + PostgreSQL");
        sousTitre.setFont(new Font("Arial", Font.ITALIC, 14));
        gbc.gridy = 1;
        panel.add(sousTitre, gbc);

        // Email
        gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.EAST;
        gbc.gridy = 2; gbc.gridx = 0;
        panel.add(new JLabel("Email:"), gbc);
        JTextField emailField = new JTextField(20);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(emailField, gbc);

        // Mot de passe
        gbc.gridy = 3; gbc.gridx = 0; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Mot de passe:"), gbc);
        JPasswordField passwordField = new JPasswordField(20);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(passwordField, gbc);

        // Boutons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton loginBtn = new JButton("Se connecter");
        JButton registerBtn = new JButton("S'inscrire");
        JButton viewClubsBtn = new JButton("Afficher les clubs");

        loginBtn.addActionListener(e -> {
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());
            seConnecter(email, password);
        });

        registerBtn.addActionListener(e -> ouvrirDialogInscription());
        viewClubsBtn.addActionListener(e -> afficherClubs());

        buttonPanel.add(loginBtn);
        buttonPanel.add(registerBtn);
        buttonPanel.add(viewClubsBtn);

        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(buttonPanel, gbc);

        return panel;
    }

    /** Connexion via l'API REST */
    private void seConnecter(String email, String password) {
        try {
            String response = ApiClient.post("/auth/login", Map.of("email", email, "password", password));
            utilisateurCourant = ApiClient.parseMap(response);
            String role = (String) utilisateurCourant.get("role");
            String nom = (String) utilisateurCourant.get("nom");

            JOptionPane.showMessageDialog(this,
                    "Connecté en tant que " + role + " : " + nom,
                    "Connexion réussie", JOptionPane.INFORMATION_MESSAGE);

            switch (role) {
                case "admin" -> afficherPanneauAdmin();
                case "president" -> afficherPanneauPresident();
                case "etudiant" -> afficherPanneauEtudiant();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Email ou mot de passe incorrect.",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Inscription via l'API REST */
    private void ouvrirDialogInscription() {
        JDialog dialog = new JDialog(this, "Inscription", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField nomField = new JTextField(20);
        JTextField prenomField = new JTextField(20);
        JTextField emailField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row++;
        panel.add(new JLabel("Nom:"), gbc);
        gbc.gridx = 1; panel.add(nomField, gbc);

        gbc.gridx = 0; gbc.gridy = row++;
        panel.add(new JLabel("Prénom:"), gbc);
        gbc.gridx = 1; panel.add(prenomField, gbc);

        gbc.gridx = 0; gbc.gridy = row++;
        panel.add(new JLabel("Email (@ihec.ucar.tn):"), gbc);
        gbc.gridx = 1; panel.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = row++;
        panel.add(new JLabel("Mot de passe:"), gbc);
        gbc.gridx = 1; panel.add(passwordField, gbc);

        JButton inscrireBtn = new JButton("S'inscrire");
        inscrireBtn.addActionListener(e -> {
            try {
                ApiClient.post("/auth/register", Map.of(
                        "nom", nomField.getText(),
                        "prenom", prenomField.getText(),
                        "email", emailField.getText(),
                        "password", new String(passwordField.getPassword())));
                JOptionPane.showMessageDialog(dialog, "Inscription réussie !",
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        panel.add(inscrireBtn, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    /** Afficher la liste des clubs via l'API */
    private void afficherClubs() {
        try {
            String response = ApiClient.get("/clubs");
            List<Map<String, Object>> clubs = ApiClient.parseList(response);
            StringBuilder sb = new StringBuilder("=== CLUBS ===\n\n");
            for (int i = 0; i < clubs.size(); i++) {
                Map<String, Object> c = clubs.get(i);
                List<?> membres = (List<?>) c.get("membres");
                sb.append((i + 1)).append(". ").append(c.get("nom"))
                  .append(" (").append(c.get("type")).append(") ")
                  .append(membres != null ? membres.size() : 0).append(" membres\n");
            }
            JOptionPane.showMessageDialog(this, sb.toString(), "Liste des Clubs",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void afficherPanneauAdmin() {
        AdminPanel adminPanel = new AdminPanel(utilisateurCourant, this);
        mainPanel.add(adminPanel, "ADMIN");
        cardLayout.show(mainPanel, "ADMIN");
    }

    private void afficherPanneauPresident() {
        PresidentPanel presidentPanel = new PresidentPanel(utilisateurCourant, this);
        mainPanel.add(presidentPanel, "PRESIDENT");
        cardLayout.show(mainPanel, "PRESIDENT");
    }

    private void afficherPanneauEtudiant() {
        EtudiantPanel etudiantPanel = new EtudiantPanel(utilisateurCourant, this);
        mainPanel.add(etudiantPanel, "ETUDIANT");
        cardLayout.show(mainPanel, "ETUDIANT");
    }

    public void deconnecter() {
        utilisateurCourant = null;
        cardLayout.show(mainPanel, "LOGIN");
    }

    public Map<String, Object> getUtilisateurCourant() {
        return utilisateurCourant;
    }
}
