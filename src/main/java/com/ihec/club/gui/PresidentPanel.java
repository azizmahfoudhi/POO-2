package com.ihec.club.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Panneau du président — gère les événements et le budget de son club.
 * Toutes les données transitent par l'API REST.
 */
public class PresidentPanel extends JPanel {

    private Map<String, Object> president;
    private MainGUI mainGUI;
    private DefaultTableModel evenementsModel;
    private JLabel soldeLabel;
    private Long clubId = null;
    private String clubNom = "";

    public PresidentPanel(Map<String, Object> president, MainGUI mainGUI) {
        this.president = president;
        this.mainGUI = mainGUI;
        setLayout(new BorderLayout());

        // Trouver le club du président
        trouverClub();

        // En-tête
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titre = new JLabel("MENU PRESIDENT : " + president.get("nom") + " — Club: " + clubNom);
        titre.setFont(new Font("Arial", Font.BOLD, 18));
        headerPanel.add(titre, BorderLayout.WEST);

        JButton deconnecterBtn = new JButton("Se déconnecter");
        deconnecterBtn.addActionListener(e -> mainGUI.deconnecter());
        headerPanel.add(deconnecterBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Événements", creerPanneauEvenements());
        tabbedPane.addTab("Planning", creerPanneauPlanning());
        tabbedPane.addTab("Budget", creerPanneauBudget());
        add(tabbedPane, BorderLayout.CENTER);
    }

    private void trouverClub() {
        try {
            String json = ApiClient.get("/clubs");
            List<Map<String, Object>> clubs = ApiClient.parseList(json);
            Long presId = ((Number) president.get("id")).longValue();
            for (Map<String, Object> club : clubs) {
                List<?> membres = (List<?>) club.get("membres");
                if (membres != null) {
                    for (Object obj : membres) {
                        Map<?, ?> m = (Map<?, ?>) obj;
                        if (((Number) m.get("id")).longValue() == presId && "president".equals(m.get("role"))) {
                            clubId = ((Number) club.get("id")).longValue();
                            clubNom = (String) club.get("nom");
                            return;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage());
        }
    }

    private JPanel creerPanneauEvenements() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] colonnes = {"Titre", "Date", "Durée", "Lieu", "Type", "Coût"};
        evenementsModel = new DefaultTableModel(colonnes, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(evenementsModel);
        actualiserEvenements();

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton ajouterBtn = new JButton("Ajouter un événement");
        JButton gererPropsBtn = new JButton("Gérer propositions");
        JButton actualiserBtn = new JButton("Actualiser");

        ajouterBtn.addActionListener(e -> ouvrirDialogAjouterEvenement());
        gererPropsBtn.addActionListener(e -> gererPropositions());
        actualiserBtn.addActionListener(e -> actualiserEvenements());

        buttonPanel.add(ajouterBtn);
        buttonPanel.add(gererPropsBtn);
        buttonPanel.add(actualiserBtn);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel creerPanneauPlanning() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea planningArea = new JTextArea();
        planningArea.setEditable(false);
        planningArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JButton afficherBtn = new JButton("Afficher planning");
        afficherBtn.addActionListener(e -> {
            if (clubId == null) return;
            try {
                String json = ApiClient.get("/clubs/" + clubId + "/evenements");
                List<Map<String, Object>> evts = ApiClient.parseList(json);
                StringBuilder sb = new StringBuilder("=== PLANNING DU CLUB : " + clubNom + " ===\n\n");
                for (Map<String, Object> evt : evts) {
                    sb.append(evt.get("titre")).append(" - ").append(evt.get("date"))
                      .append(" (").append(evt.get("duree")).append("h) | Lieu: ")
                      .append(evt.get("lieu")).append(" | [").append(evt.get("typeEvenement"))
                      .append("] Coût: ").append(String.format("%.2f", ((Number) evt.get("coutTotal")).doubleValue()))
                      .append("\n");
                }
                if (evts.isEmpty()) sb.append("Aucun événement.\n");
                planningArea.setText(sb.toString());
            } catch (Exception ex) {
                planningArea.setText("Erreur: " + ex.getMessage());
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.add(afficherBtn);
        panel.add(new JScrollPane(planningArea), BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel creerPanneauBudget() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel titreLabel = new JLabel("Gestion du Budget");
        titreLabel.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(titreLabel, gbc);

        gbc.gridwidth = 1; gbc.gridy = 1; gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Solde actuel:"), gbc);
        soldeLabel = new JLabel("—");
        soldeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        soldeLabel.setForeground(new Color(0, 100, 0));
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(soldeLabel, gbc);
        actualiserSolde();

        JButton definirBtn = new JButton("Définir le budget");
        definirBtn.addActionListener(e -> {
            if (clubId == null) return;
            String input = JOptionPane.showInputDialog(this, "Nouveau solde:");
            if (input != null) {
                try {
                    double solde = Double.parseDouble(input);
                    ApiClient.put("/clubs/" + clubId + "/budget", Map.of("solde", solde));
                    actualiserSolde();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage());
                }
            }
        });
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(definirBtn, gbc);

        JButton actualiserBtn = new JButton("Actualiser");
        actualiserBtn.addActionListener(e -> actualiserSolde());
        gbc.gridy = 3;
        panel.add(actualiserBtn, gbc);

        return panel;
    }

    private void actualiserEvenements() {
        evenementsModel.setRowCount(0);
        if (clubId == null) return;
        try {
            String json = ApiClient.get("/clubs/" + clubId + "/evenements");
            List<Map<String, Object>> evts = ApiClient.parseList(json);
            for (Map<String, Object> e : evts) {
                evenementsModel.addRow(new Object[]{
                    e.get("titre"), e.get("date"), e.get("duree") + "h",
                    e.get("lieu"), e.get("typeEvenement"),
                    String.format("%.2f DT", ((Number) e.get("coutTotal")).doubleValue())
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage());
        }
    }

    private void actualiserSolde() {
        if (clubId == null) return;
        try {
            String json = ApiClient.get("/clubs/" + clubId + "/budget");
            Map<String, Object> budget = ApiClient.parseMap(json);
            soldeLabel.setText(String.format("%.2f DT", ((Number) budget.get("solde")).doubleValue()));
        } catch (Exception ex) {
            soldeLabel.setText("Erreur");
        }
    }

    private void ouvrirDialogAjouterEvenement() {
        if (clubId == null) return;
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Ajouter un Événement", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField titreField = new JTextField(20);
        JTextField dateField = new JTextField(20);
        JTextField dureeField = new JTextField(20);
        JTextField lieuField = new JTextField(20);
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"academique", "sportif", "culturel"});
        JTextField coutField = new JTextField(20);

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row++; panel.add(new JLabel("Titre:"), gbc);
        gbc.gridx = 1; panel.add(titreField, gbc);
        gbc.gridx = 0; gbc.gridy = row++; panel.add(new JLabel("Date (JJ/MM/AAAA):"), gbc);
        gbc.gridx = 1; panel.add(dateField, gbc);
        gbc.gridx = 0; gbc.gridy = row++; panel.add(new JLabel("Durée (heures):"), gbc);
        gbc.gridx = 1; panel.add(dureeField, gbc);
        gbc.gridx = 0; gbc.gridy = row++; panel.add(new JLabel("Lieu:"), gbc);
        gbc.gridx = 1; panel.add(lieuField, gbc);
        gbc.gridx = 0; gbc.gridy = row++; panel.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1; panel.add(typeCombo, gbc);
        gbc.gridx = 0; gbc.gridy = row++; panel.add(new JLabel("Coût:"), gbc);
        gbc.gridx = 1; panel.add(coutField, gbc);

        JButton ajouterBtn = new JButton("Ajouter");
        ajouterBtn.addActionListener(e -> {
            try {
                String type = (String) typeCombo.getSelectedItem();
                Map<String, Object> body = new HashMap<>();
                body.put("titre", titreField.getText());
                body.put("date", dateField.getText());
                body.put("duree", Integer.parseInt(dureeField.getText()));
                body.put("lieu", lieuField.getText());
                body.put("type", type);
                double cout = Double.parseDouble(coutField.getText());
                switch (type) {
                    case "academique" -> body.put("coutGlobal", cout);
                    case "sportif" -> body.put("coutEquip", cout);
                    case "culturel" -> body.put("coutDeco", cout);
                }
                ApiClient.post("/clubs/" + clubId + "/evenements", body);
                actualiserEvenements();
                actualiserSolde();
                JOptionPane.showMessageDialog(dialog, "Événement ajouté !");
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Erreur: " + ex.getMessage());
            }
        });
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(ajouterBtn, gbc);
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void gererPropositions() {
        if (clubId == null) return;
        try {
            String json = ApiClient.get("/clubs/" + clubId + "/propositions");
            List<Map<String, Object>> props = ApiClient.parseList(json);
            if (props.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Aucune proposition en attente.");
                return;
            }

            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Propositions", true);
            dialog.setSize(700, 400);
            dialog.setLocationRelativeTo(this);

            String[] cols = {"Titre", "Date", "Durée", "Lieu", "Proposé par"};
            DefaultTableModel model = new DefaultTableModel(cols, 0);
            for (Map<String, Object> p : props) {
                Map<?, ?> proposeur = (Map<?, ?>) p.get("proposeur");
                String nom = proposeur != null ? proposeur.get("nom") + " " + proposeur.get("prenom") : "—";
                model.addRow(new Object[]{p.get("titre"), p.get("date"), p.get("duree") + "h", p.get("lieu"), nom});
            }
            JTable table = new JTable(model);

            JPanel btnPanel = new JPanel(new FlowLayout());
            JButton accepterBtn = new JButton("Accepter");
            JButton refuserBtn = new JButton("Refuser");

            accepterBtn.addActionListener(e -> {
                int r = table.getSelectedRow();
                if (r == -1) return;
                try {
                    ApiClient.put("/propositions/" + props.get(r).get("id") + "/accepter", Map.of());
                    model.removeRow(r); props.remove(r);
                    actualiserEvenements(); actualiserSolde();
                    if (model.getRowCount() == 0) dialog.dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Erreur: " + ex.getMessage());
                }
            });
            refuserBtn.addActionListener(e -> {
                int r = table.getSelectedRow();
                if (r == -1) return;
                try {
                    ApiClient.put("/propositions/" + props.get(r).get("id") + "/refuser", Map.of());
                    model.removeRow(r); props.remove(r);
                    if (model.getRowCount() == 0) dialog.dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Erreur: " + ex.getMessage());
                }
            });

            btnPanel.add(accepterBtn);
            btnPanel.add(refuserBtn);
            dialog.add(new JScrollPane(table), BorderLayout.CENTER);
            dialog.add(btnPanel, BorderLayout.SOUTH);
            dialog.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage());
        }
    }
}
