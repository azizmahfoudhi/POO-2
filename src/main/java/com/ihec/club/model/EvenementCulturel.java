package com.ihec.club.model;

import jakarta.persistence.*;

/**
 * Événement culturel — coût = décoration + audio.
 * Démontre : Polymorphisme (getCoutTotal = coutDeco + coutAudio)
 */
@Entity
@DiscriminatorValue("culturel")
public class EvenementCulturel extends Activite {

    private double coutDeco;
    private double coutAudio;

    public EvenementCulturel() {}

    public EvenementCulturel(String titre, String date, int duree, String lieu, double coutDeco, double coutAudio) {
        super(titre, date, duree, lieu);
        this.coutDeco = coutDeco;
        this.coutAudio = coutAudio;
    }

    @Override
    public double getCoutTotal() { return coutDeco + coutAudio; }

    public double getCoutDeco() { return coutDeco; }
    public void setCoutDeco(double coutDeco) { this.coutDeco = coutDeco; }

    public double getCoutAudio() { return coutAudio; }
    public void setCoutAudio(double coutAudio) { this.coutAudio = coutAudio; }

    @Override
    public String toString() {
        return super.toString() + " | [Culturel] Coût: " + getCoutTotal();
    }
}
