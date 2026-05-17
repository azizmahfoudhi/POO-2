package com.ihec.club.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

/**
 * Classe de base pour tous les membres du système.
 * Utilise l'héritage SINGLE_TABLE : Admin, President et Etudiant
 * sont stockés dans la même table avec un discriminateur "role".
 *
 * Démontre : Encapsulation, Héritage, Polymorphisme
 */
@Entity
@Table(name = "membre")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "role")
public class Membre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String prenom;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    /** Champ en lecture seule mappé sur la colonne discriminateur */
    @Column(name = "role", insertable = false, updatable = false)
    private String role;

    // ===== Constructeurs =====
    protected Membre() {}

    public Membre(String nom, String prenom, String email, String password) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.password = password;
    }

    // ===== Méthodes métier =====

    /**
     * Vérifie les identifiants de connexion.
     * @return true si l'email et le mot de passe correspondent
     */
    public boolean authentifier(String email, String mdp) {
        return this.email.equals(email) && this.password.equals(mdp);
    }

    // ===== Getters & Setters =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @JsonIgnore
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
}
