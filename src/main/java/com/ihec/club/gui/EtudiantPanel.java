package com.ihec.club.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.table.TableRowSorter;

/**
 * Panneau étudiant — consulte les événements et propose des activités.
 * Toutes les données transitent par l'API REST.
 */
public class EtudiantPanel extends JPanel {

    private Map<String, Object> etudiant;
    private MainGUI mainGUI;
    private DefaultTableModel evenementsModel;

    public EtudiantPanel(Map<String, Object> etudiant, MainGUI mainGUI) {
        this.etudiant = etudiant;
        this.mainGUI = mainGUI;
        setLayout(new BorderLayout());

        // En-tête
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        headerPanel.setBackground(new Color(41, 128, 185));
        JLabel titre = new JLabel("Bienvenue, " + etudiant.get("prenom") + " " + etudiant.get("nom") + "  🎓");
        titre.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titre.setForeground(Color.WHITE);
        headerPanel.add(titre, BorderLayout.WEST);

        JButton deconnecterBtn = new JButton("Se déconnecter");
        deconnecterBtn.setFocusPainted(false);
        deconnecterBtn.addActionListener(e -> mainGUI.deconnecter());
        headerPanel.add(deconnecterBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Consulter les Événements", creerPanneauEvenements());
        tabbedPane.addTab("Proposer un Événement", creerPanneauProposer());
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel creerPanneauEvenements() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Sans les coûts pour les étudiants
        String[] colonnes = {"Club", "Titre", "Date", "Durée", "Lieu", "Type"};
        evenementsModel = new DefaultTableModel(colonnes, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(evenementsModel);
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(evenementsModel);
        table.setRowSorter(sorter);

        // Search bar
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Rechercher un événement : "));
        JTextField searchField = new JTextField(22);
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(searchField.getText(), sorter); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(searchField.getText(), sorter); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(searchField.getText(), sorter); }
        });
        searchPanel.add(searchField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton actualiserBtn = new JButton("Actualiser");
        actualiserBtn.addActionListener(e -> actualiserEvenements());
        buttonPanel.add(actualiserBtn);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(buttonPanel, BorderLayout.EAST);

        actualiserEvenements();

        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(southPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void filter(String str, TableRowSorter<DefaultTableModel> sorter) {
        if (str.trim().isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + str));
        }
    }

    private JPanel creerPanneauProposer() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Sélection du club
        JComboBox<String> clubCombo = new JComboBox<>();
        List<Map<String, Object>> clubsList = new ArrayList<>();
        try {
            String json = ApiClient.get("/clubs");
            clubsList.addAll(ApiClient.parseList(json));
            for (Map<String, Object> c : clubsList) {
                clubCombo.addItem(c.get("nom") + " (" + c.get("type") + ")");
            }
        } catch (Exception ex) {
            // silently fail, combo will be empty
        }

        JTextField titreField = new JTextField(20);
        JTextField dateField = new JTextField(20);
        JTextField dureeField = new JTextField(20);
        JTextField lieuField = new JTextField(20);
        JTextArea explicationArea = new JTextArea(5, 20);
        explicationArea.setLineWrap(true);
        explicationArea.setWrapStyleWord(true);

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row++;
        panel.add(new JLabel("Sélectionner un club:"), gbc);
        gbc.gridx = 1; panel.add(clubCombo, gbc);

        gbc.gridx = 0; gbc.gridy = row++;
        panel.add(new JLabel("Titre:"), gbc);
        gbc.gridx = 1; panel.add(titreField, gbc);

        gbc.gridx = 0; gbc.gridy = row++;
        panel.add(new JLabel("Date (JJ/MM/AAAA):"), gbc);
        gbc.gridx = 1; panel.add(dateField, gbc);

        gbc.gridx = 0; gbc.gridy = row++;
        panel.add(new JLabel("Durée (heures):"), gbc);
        gbc.gridx = 1; panel.add(dureeField, gbc);

        gbc.gridx = 0; gbc.gridy = row++;
        panel.add(new JLabel("Lieu:"), gbc);
        gbc.gridx = 1; panel.add(lieuField, gbc);

        gbc.gridx = 0; gbc.gridy = row++;
        panel.add(new JLabel("Explication:"), gbc);
        gbc.gridx = 1; panel.add(new JScrollPane(explicationArea), gbc);

        // Store clubs list reference for the button
        final List<Map<String, Object>> clubsRef = clubsList;

        JButton proposerBtn = new JButton("Proposer l'événement");
        proposerBtn.addActionListener(e -> {
            int idx = clubCombo.getSelectedIndex();
            if (idx < 0 || idx >= clubsRef.size()) {
                JOptionPane.showMessageDialog(this, "Sélectionnez un club");
                return;
            }
            try {
                Map<String, Object> club = clubsRef.get(idx);
                Map<String, Object> body = new HashMap<>();
                body.put("membreId", ((Number) etudiant.get("id")).longValue());
                body.put("titre", titreField.getText());
                body.put("date", dateField.getText());
                body.put("duree", Integer.parseInt(dureeField.getText()));
                body.put("lieu", lieuField.getText());
                body.put("explication", explicationArea.getText());

                ApiClient.post("/clubs/" + club.get("id") + "/propositions", body);
                JOptionPane.showMessageDialog(this, "Proposition envoyée avec succès !");

                // Réinitialiser les champs
                titreField.setText("");
                dateField.setText("");
                dureeField.setText("");
                lieuField.setText("");
                explicationArea.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage());
            }
        });

        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        panel.add(proposerBtn, gbc);

        return panel;
    }

    private void actualiserEvenements() {
        evenementsModel.setRowCount(0);
        try {
            // Get all clubs with their events
            String json = ApiClient.get("/clubs");
            List<Map<String, Object>> clubs = ApiClient.parseList(json);
            for (Map<String, Object> club : clubs) {
                List<?> evts = (List<?>) club.get("evenements");
                if (evts == null) continue;
                for (Object obj : evts) {
                    Map<?, ?> e = (Map<?, ?>) obj;
                    // Only show approved events
                    if (!"APPROUVE".equals(e.get("statut"))) continue;
                    evenementsModel.addRow(new Object[]{
                        club.get("nom"),
                        e.get("titre"),
                        e.get("date"),
                        e.get("duree") + "h",
                        e.get("lieu"),
                        e.get("typeEvenement") != null ? e.get("typeEvenement") : ""
                    });
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage());
        }
    }
}
