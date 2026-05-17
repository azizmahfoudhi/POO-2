package com.ihec.club.model;

import jakarta.persistence.*;

/**
 * Président d'un club — gère les événements et le budget de son club.
 * Hérite de Membre (Héritage).
 */
@Entity
@DiscriminatorValue("president")
public class President extends Membre {

    public President() {}

    public President(String nom, String prenom, String email, String password) {
        super(nom, prenom, email, password);
    }
}
