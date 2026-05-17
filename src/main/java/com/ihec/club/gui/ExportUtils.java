package com.ihec.club.gui;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ExportUtils {

    public static void exportTableToCSV(JTable table, JPanel parentPanel) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Exporter en CSV");
        int userSelection = fileChooser.showSaveDialog(parentPanel);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            // Assurez-vous que le fichier se termine par .csv
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".csv")) {
                filePath += ".csv";
            }

            try (FileWriter fw = new FileWriter(filePath)) {
                TableModel model = table.getModel();
                
                // En-têtes
                for (int i = 0; i < model.getColumnCount(); i++) {
                    fw.write(model.getColumnName(i) + (i == model.getColumnCount() - 1 ? "" : ","));
                }
                fw.write("\n");
                
                // Lignes
                for (int i = 0; i < model.getRowCount(); i++) {
                    for (int j = 0; j < model.getColumnCount(); j++) {
                        Object val = model.getValueAt(i, j);
                        String strVal = val != null ? val.toString().replace(",", ";") : ""; // Eviter les problèmes de virgules
                        fw.write(strVal + (j == model.getColumnCount() - 1 ? "" : ","));
                    }
                    fw.write("\n");
                }
                
                JOptionPane.showMessageDialog(parentPanel, "Exportation réussie !\n" + filePath, "Succès", JOptionPane.INFORMATION_MESSAGE);
                
            } catch (IOException e) {
                JOptionPane.showMessageDialog(parentPanel, "Erreur lors de l'exportation : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
