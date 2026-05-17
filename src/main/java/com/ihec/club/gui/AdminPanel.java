package com.ihec.club.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Panneau d'administration — accès complet à tous les clubs.
 * Toutes les données transitent par l'API REST.
 */
public class AdminPanel extends JPanel {

    private Map<String, Object> admin;
    private MainGUI mainGUI;
    private JComboBox<String> clubComboBox;
    private List<Map<String, Object>> clubsData = new ArrayList<>();
    private DefaultTableModel membresModel;
    private DefaultTableModel evenementsModel;
    private int clubSelectionneIndex = -1;

    public AdminPanel(Map<String, Object> admin, MainGUI mainGUI) {
        this.admin = admin;
        this.mainGUI = mainGUI;
        setLayout(new BorderLayout());

        // En-tête
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titre = new JLabel("MENU ADMIN : " + admin.get("nom"));
        titre.setFont(new Font("Arial", Font.BOLD, 18));
        headerPanel.add(titre, BorderLayout.WEST);

        JButton deconnecterBtn = new JButton("Se déconnecter");
        deconnecterBtn.addActionListener(e -> mainGUI.deconnecter());
        headerPanel.add(deconnecterBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Gestion des Clubs", creerPanneauGestionClubs());
        tabbedPane.addTab("Gérer un Club", creerPanneauGererClub());
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel creerPanneauGestionClubs() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] colonnes = {"Nom", "Type", "Membres", "Budget"};
        DefaultTableModel model = new DefaultTableModel(colonnes, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton ajouterBtn = new JButton("Ajouter un club");
        JButton actualiserBtn = new JButton("Actualiser");
        JButton supprimerBtn = new JButton("Supprimer le club");

        ajouterBtn.addActionListener(e -> ouvrirDialogAjouterClub(model));
        actualiserBtn.addActionListener(e -> actualiserTableClubs(model));
        supprimerBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Sélectionnez un club");
                return;
            }
            try {
                Map<String, Object> club = clubsData.get(row);
                int confirm = JOptionPane.showConfirmDialog(this, "Supprimer le club " + club.get("nom") + " ?");
                if (confirm == JOptionPane.YES_OPTION) {
                    ApiClient.delete("/clubs/" + club.get("id"));
                    actualiserTableClubs(model);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage());
            }
        });

        buttonPanel.add(ajouterBtn);
        buttonPanel.add(supprimerBtn);
        buttonPanel.add(actualiserBtn);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        actualiserTableClubs(model);
        return panel;
    }

    private void actualiserTableClubs(DefaultTableModel model) {
        try {
            model.setRowCount(0);
            String json = ApiClient.get("/clubs");
            clubsData = ApiClient.parseList(json);
            for (Map<String, Object> c : clubsData) {
                List<?> membres = (List<?>) c.get("membres");
                Map<?, ?> budget = (Map<?, ?>) c.get("budget");
                model.addRow(new Object[]{
                    c.get("nom"), c.get("type"),
                    membres != null ? membres.size() : 0,
                    budget != null ? String.format("%.2f DT", ((Number) budget.get("solde")).doubleValue()) : "N/A"
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage());
        }
    }

    private JPanel creerPanneauGererClub() {
        JPanel panel = new JPanel(new BorderLayout());

        // Sélection du club
        JPanel selectionPanel = new JPanel(new FlowLayout());
        selectionPanel.add(new JLabel("Sélectionner un club:"));
        clubComboBox = new JComboBox<>();
        actualiserComboBox();
        clubComboBox.addActionListener(e -> {
            clubSelectionneIndex = clubComboBox.getSelectedIndex();
            actualiserMembres();
            actualiserEvenements();
        });
        selectionPanel.add(clubComboBox);

        JTabbedPane innerTabs = new JTabbedPane();

        // --- Onglet Membres ---
        JPanel membresPanel = new JPanel(new BorderLayout());
        String[] colMembres = {"Nom", "Prénom", "Email", "Rôle"};
        membresModel = new DefaultTableModel(colMembres, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable membresTable = new JTable(membresModel);
        JPanel membresBtnPanel = new JPanel(new FlowLayout());
        JButton ajouterMembreBtn = new JButton("Ajouter un membre");
        JButton supprimerMembreBtn = new JButton("Supprimer");
        JButton actualiserMembresBtn = new JButton("Actualiser");

        ajouterMembreBtn.addActionListener(e -> ouvrirDialogAjouterMembre());
        supprimerMembreBtn.addActionListener(e -> {
            int row = membresTable.getSelectedRow();
            if (row == -1 || clubSelectionneIndex < 0) return;
            try {
                Map<String, Object> club = clubsData.get(clubSelectionneIndex);
                List<?> membres = (List<?>) club.get("membres");
                Map<?, ?> m = (Map<?, ?>) membres.get(row);
                ApiClient.delete("/clubs/" + club.get("id") + "/membres/" + m.get("id"));
                actualiserComboBox();
                actualiserMembres();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage());
            }
        });
        actualiserMembresBtn.addActionListener(e -> { actualiserComboBox(); actualiserMembres(); });

        membresBtnPanel.add(ajouterMembreBtn);
        membresBtnPanel.add(supprimerMembreBtn);
        membresBtnPanel.add(actualiserMembresBtn);
        membresPanel.add(new JScrollPane(membresTable), BorderLayout.CENTER);
        membresPanel.add(membresBtnPanel, BorderLayout.SOUTH);
        innerTabs.addTab("Membres", membresPanel);

        // --- Onglet Événements ---
        JPanel evenementsPanel = new JPanel(new BorderLayout());
        String[] colEvts = {"Titre", "Date", "Durée", "Lieu", "Type", "Coût"};
        evenementsModel = new DefaultTableModel(colEvts, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable evtsTable = new JTable(evenementsModel);
        JPanel evtsBtnPanel = new JPanel(new FlowLayout());
        JButton ajouterEvtBtn = new JButton("Ajouter un événement");
        JButton gererPropsBtn = new JButton("Gérer propositions");
        JButton actualiserEvtsBtn = new JButton("Actualiser");

        ajouterEvtBtn.addActionListener(e -> ouvrirDialogAjouterEvenement());
        gererPropsBtn.addActionListener(e -> gererPropositions());
        actualiserEvtsBtn.addActionListener(e -> actualiserEvenements());

        evtsBtnPanel.add(ajouterEvtBtn);
        evtsBtnPanel.add(gererPropsBtn);
        evtsBtnPanel.add(actualiserEvtsBtn);
        evenementsPanel.add(new JScrollPane(evtsTable), BorderLayout.CENTER);
        evenementsPanel.add(evtsBtnPanel, BorderLayout.SOUTH);
        innerTabs.addTab("Événements", evenementsPanel);

        // --- Onglet Budget ---
        JPanel budgetPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        JLabel soldeLabel = new JLabel("Solde actuel:");
        soldeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        JLabel soldeValue = new JLabel("—");
        soldeValue.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 0; gbc.gridy = 0; budgetPanel.add(soldeLabel, gbc);
        gbc.gridx = 1; budgetPanel.add(soldeValue, gbc);

        JButton definirBudgetBtn = new JButton("Définir le budget");
        definirBudgetBtn.addActionListener(e -> {
            if (clubSelectionneIndex < 0) return;
            try {
                Map<String, Object> club = clubsData.get(clubSelectionneIndex);
                String input = JOptionPane.showInputDialog(this, "Nouveau solde:", "Définir le budget", JOptionPane.QUESTION_MESSAGE);
                if (input != null) {
                    double solde = Double.parseDouble(input);
                    ApiClient.put("/clubs/" + club.get("id") + "/budget", Map.of("solde", solde));
                    actualiserComboBox();
                    soldeValue.setText(String.format("%.2f DT", solde));
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage());
            }
        });
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        budgetPanel.add(definirBudgetBtn, gbc);

        // Update solde when club changes
        clubComboBox.addActionListener(e -> {
            if (clubSelectionneIndex >= 0 && clubSelectionneIndex < clubsData.size()) {
                Map<?, ?> budget = (Map<?, ?>) clubsData.get(clubSelectionneIndex).get("budget");
                if (budget != null) {
                    soldeValue.setText(String.format("%.2f DT", ((Number) budget.get("solde")).doubleValue()));
                }
            }
        });

        innerTabs.addTab("Budget", budgetPanel);

        panel.add(selectionPanel, BorderLayout.NORTH);
        panel.add(innerTabs, BorderLayout.CENTER);
        return panel;
    }

    private void actualiserComboBox() {
        try {
            String json = ApiClient.get("/clubs");
            clubsData = ApiClient.parseList(json);
            clubComboBox.removeAllItems();
            for (Map<String, Object> c : clubsData) {
                clubComboBox.addItem(c.get("nom") + " (" + c.get("type") + ")");
            }
            if (!clubsData.isEmpty()) clubSelectionneIndex = 0;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage());
        }
    }

    private void actualiserMembres() {
        membresModel.setRowCount(0);
        if (clubSelectionneIndex < 0 || clubSelectionneIndex >= clubsData.size()) return;
        List<?> membres = (List<?>) clubsData.get(clubSelectionneIndex).get("membres");
        if (membres == null) return;
        for (Object obj : membres) {
            Map<?, ?> m = (Map<?, ?>) obj;
            membresModel.addRow(new Object[]{m.get("nom"), m.get("prenom"), m.get("email"), m.get("role")});
        }
    }

    private void actualiserEvenements() {
        evenementsModel.setRowCount(0);
        if (clubSelectionneIndex < 0 || clubSelectionneIndex >= clubsData.size()) return;
        List<?> evts = (List<?>) clubsData.get(clubSelectionneIndex).get("evenements");
        if (evts == null) return;
        for (Object obj : evts) {
            Map<?, ?> e = (Map<?, ?>) obj;
            String type = e.get("typeEvenement") != null ? (String) e.get("typeEvenement") : "";
            double cout = e.get("coutTotal") != null ? ((Number) e.get("coutTotal")).doubleValue() : 0;
            evenementsModel.addRow(new Object[]{
                e.get("titre"), e.get("date"), e.get("duree") + "h",
                e.get("lieu"), type, String.format("%.2f DT", cout)
            });
        }
    }

    private void ouvrirDialogAjouterClub(DefaultTableModel tableModel) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Ajouter un Club", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField nomField = new JTextField(20);
        JTextField typeField = new JTextField(20);
        JTextField budgetField = new JTextField(20);
        int row = 0;
        gbc.gridx = 0; gbc.gridy = row++; panel.add(new JLabel("Nom du club:"), gbc);
        gbc.gridx = 1; panel.add(nomField, gbc);
        gbc.gridx = 0; gbc.gridy = row++; panel.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1; panel.add(typeField, gbc);
        gbc.gridx = 0; gbc.gridy = row++; panel.add(new JLabel("Budget initial:"), gbc);
        gbc.gridx = 1; panel.add(budgetField, gbc);

        JButton creerBtn = new JButton("Créer");
        creerBtn.addActionListener(e -> {
            try {
                ApiClient.post("/clubs", Map.of(
                    "nom", nomField.getText(),
                    "type", typeField.getText(),
                    "budget", Double.parseDouble(budgetField.getText())));
                actualiserTableClubs(tableModel);
                actualiserComboBox();
                JOptionPane.showMessageDialog(dialog, "Club créé !");
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Erreur: " + ex.getMessage());
            }
        });
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        panel.add(creerBtn, gbc);
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void ouvrirDialogAjouterMembre() {
        if (clubSelectionneIndex < 0) return;
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Ajouter un Membre", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField nomField = new JTextField(20);
        JTextField prenomField = new JTextField(20);
        JTextField emailField = new JTextField(20);
        JPasswordField pwField = new JPasswordField(20);
        int row = 0;
        gbc.gridx = 0; gbc.gridy = row++; panel.add(new JLabel("Nom:"), gbc);
        gbc.gridx = 1; panel.add(nomField, gbc);
        gbc.gridx = 0; gbc.gridy = row++; panel.add(new JLabel("Prénom:"), gbc);
        gbc.gridx = 1; panel.add(prenomField, gbc);
        gbc.gridx = 0; gbc.gridy = row++; panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; panel.add(emailField, gbc);
        gbc.gridx = 0; gbc.gridy = row++; panel.add(new JLabel("Mot de passe:"), gbc);
        gbc.gridx = 1; panel.add(pwField, gbc);

        JButton ajouterBtn = new JButton("Ajouter");
        ajouterBtn.addActionListener(e -> {
            try {
                // 1. Register the member
                String resp = ApiClient.post("/auth/register", Map.of(
                    "nom", nomField.getText(), "prenom", prenomField.getText(),
                    "email", emailField.getText(), "password", new String(pwField.getPassword())));
                Map<String, Object> newMembre = ApiClient.parseMap(resp);
                // 2. Add to club
                Map<String, Object> club = clubsData.get(clubSelectionneIndex);
                ApiClient.post("/clubs/" + club.get("id") + "/membres/" + newMembre.get("id"), Map.of());
                actualiserComboBox();
                actualiserMembres();
                JOptionPane.showMessageDialog(dialog, "Membre ajouté !");
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Erreur: " + ex.getMessage());
            }
        });
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        panel.add(ajouterBtn, gbc);
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void ouvrirDialogAjouterEvenement() {
        if (clubSelectionneIndex < 0) return;
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
                Map<String, Object> club = clubsData.get(clubSelectionneIndex);
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
                ApiClient.post("/clubs/" + club.get("id") + "/evenements", body);
                actualiserComboBox();
                actualiserEvenements();
                JOptionPane.showMessageDialog(dialog, "Événement ajouté !");
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Erreur: " + ex.getMessage());
            }
        });
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        panel.add(ajouterBtn, gbc);
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void gererPropositions() {
        if (clubSelectionneIndex < 0) return;
        try {
            Map<String, Object> club = clubsData.get(clubSelectionneIndex);
            String json = ApiClient.get("/clubs/" + club.get("id") + "/propositions");
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
                String proposeurNom = proposeur != null ? proposeur.get("nom") + " " + proposeur.get("prenom") : "—";
                model.addRow(new Object[]{p.get("titre"), p.get("date"), p.get("duree") + "h", p.get("lieu"), proposeurNom});
            }
            JTable table = new JTable(model);

            JPanel btnPanel = new JPanel(new FlowLayout());
            JButton accepterBtn = new JButton("Accepter");
            JButton refuserBtn = new JButton("Refuser");

            accepterBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row == -1) return;
                try {
                    ApiClient.put("/propositions/" + props.get(row).get("id") + "/accepter", Map.of());
                    model.removeRow(row);
                    props.remove(row);
                    actualiserComboBox();
                    actualiserEvenements();
                    if (model.getRowCount() == 0) dialog.dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Erreur: " + ex.getMessage());
                }
            });
            refuserBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row == -1) return;
                try {
                    ApiClient.put("/propositions/" + props.get(row).get("id") + "/refuser", Map.of());
                    model.removeRow(row);
                    props.remove(row);
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
