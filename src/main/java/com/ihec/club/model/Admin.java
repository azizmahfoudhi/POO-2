package com.ihec.club.model;

import jakarta.persistence.*;

/**
 * Administrateur du système — accès complet à tous les clubs.
 * Hérite de Membre (Héritage).
 */
@Entity
@DiscriminatorValue("admin")
public class Admin extends Membre {

    public Admin() {}

    public Admin(String nom, String prenom, String email, String password) {
        super(nom, prenom, email, password);
    }
}
