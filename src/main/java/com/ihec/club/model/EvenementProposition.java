package com.ihec.club.model;

import jakarta.persistence.*;

/**
 * Proposition d'événement par un étudiant — coût = 0 (pas encore approuvé).
 * Démontre : Polymorphisme (getCoutTotal retourne 0)
 */
@Entity
@DiscriminatorValue("proposition")
public class EvenementProposition extends Activite {

    private String explication;

    public EvenementProposition() {}

    public EvenementProposition(String titre, String date, int duree, String lieu, String explication) {
        super(titre, date, duree, lieu);
        this.explication = explication;
        setStatut("EN_ATTENTE"); // Les propositions sont en attente par défaut
    }

    @Override
    public double getCoutTotal() { return 0; }

    public String getExplication() { return explication; }
    public void setExplication(String explication) { this.explication = explication; }

    @Override
    public String toString() {
        return super.toString() + " | [Proposition] " + explication;
    }
}
