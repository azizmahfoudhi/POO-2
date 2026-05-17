package com.ihec.club.model;

import jakarta.persistence.*;

/**
 * Budget d'un club — gère le solde financier.
 * Démontre : Encapsulation (champs privés + getters/setters)
 */
@Entity
@Table(name = "budget")
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double solde;

    // ===== Constructeurs =====
    public Budget() {}

    public Budget(double solde) {
        this.solde = solde;
    }

    // ===== Méthodes métier =====

    public double afficherSolde() { return solde; }

    public void ajouterDepense(double montant) { solde -= montant; }

    public void ajouterRevenu(double montant) { solde += montant; }

    public boolean aSuffisammentDeFonds(double montant) { return solde >= montant; }

    // ===== Getters & Setters =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public double getSolde() { return solde; }
    public void setSolde(double solde) { this.solde = solde; }
}
