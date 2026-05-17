package com.ihec.club.model;

import jakarta.persistence.*;

/**
 * Étudiant — peut consulter les événements et proposer des activités.
 * Hérite de Membre (Héritage).
 */
@Entity
@DiscriminatorValue("etudiant")
public class Etudiant extends Membre {

    public Etudiant() {}

    public Etudiant(String nom, String prenom, String email, String password) {
        super(nom, prenom, email, password);
    }
}
