package com.ihec.club.model;

import jakarta.persistence.*;

/**
 * Événement académique — coût global unique.
 * Démontre : Polymorphisme (getCoutTotal retourne coutGlobal)
 */
@Entity
@DiscriminatorValue("academique")
public class EvenementAcademique extends Activite {

    private double coutGlobal;

    public EvenementAcademique() {}

    public EvenementAcademique(String titre, String date, int duree, String lieu, double coutGlobal) {
        super(titre, date, duree, lieu);
        this.coutGlobal = coutGlobal;
    }

    @Override
    public double getCoutTotal() { return coutGlobal; }

    public double getCoutGlobal() { return coutGlobal; }
    public void setCoutGlobal(double coutGlobal) { this.coutGlobal = coutGlobal; }

    @Override
    public String toString() {
        return super.toString() + " | [Académique] Coût: " + getCoutTotal();
    }
}
