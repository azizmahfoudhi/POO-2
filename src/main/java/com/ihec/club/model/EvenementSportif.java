package com.ihec.club.model;

import jakarta.persistence.*;

/**
 * Événement sportif — coût = équipement + logistique.
 * Démontre : Polymorphisme (getCoutTotal = coutEquip + coutLog)
 */
@Entity
@DiscriminatorValue("sportif")
public class EvenementSportif extends Activite {

    private double coutEquip;
    private double coutLog;

    public EvenementSportif() {}

    public EvenementSportif(String titre, String date, int duree, String lieu, double coutEquip, double coutLog) {
        super(titre, date, duree, lieu);
        this.coutEquip = coutEquip;
        this.coutLog = coutLog;
    }

    @Override
    public double getCoutTotal() { return coutEquip + coutLog; }

    public double getCoutEquip() { return coutEquip; }
    public void setCoutEquip(double coutEquip) { this.coutEquip = coutEquip; }

    public double getCoutLog() { return coutLog; }
    public void setCoutLog(double coutLog) { this.coutLog = coutLog; }

    @Override
    public String toString() {
        return super.toString() + " | [Sportif] Coût: " + getCoutTotal();
    }
}
