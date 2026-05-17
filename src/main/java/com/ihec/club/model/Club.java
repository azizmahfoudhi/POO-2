package com.ihec.club.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Club universitaire — entité centrale du système.
 * Démontre : Composition (Budget), Association (Membres, Événements)
 */
@Entity
@Table(name = "club")
public class Club {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    private String type;

    /** Composition : un club possède un budget */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "budget_id")
    private Budget budget;

    /** Association : un club contient plusieurs membres */
    @ManyToMany
    @JoinTable(
        name = "club_membres",
        joinColumns = @JoinColumn(name = "club_id"),
        inverseJoinColumns = @JoinColumn(name = "membre_id")
    )
    @JsonIgnoreProperties({"password"})
    private List<Membre> membres = new ArrayList<>();

    /** Association : un club organise plusieurs événements */
    @OneToMany(mappedBy = "club", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"club"})
    private List<Activite> evenements = new ArrayList<>();

    // ===== Constructeurs =====
    public Club() {}

    public Club(String nom, String type, Budget budget) {
        this.nom = nom;
        this.type = type;
        this.budget = budget;
    }

    // ===== Méthodes métier =====

    public void ajouterMembre(Membre m) {
        if (!membres.contains(m)) {
            membres.add(m);
        }
    }

    public void supprimerMembre(String email) {
        membres.removeIf(m -> m.getEmail().equals(email));
    }

    public void ajouterEvenement(Activite e) {
        evenements.add(e);
        e.setClub(this);
    }

    @Override
    public String toString() {
        return nom + " (" + type + ")";
    }

    // ===== Getters & Setters =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Budget getBudget() { return budget; }
    public void setBudget(Budget budget) { this.budget = budget; }

    public List<Membre> getMembres() { return membres; }
    public void setMembres(List<Membre> membres) { this.membres = membres; }

    public List<Activite> getEvenements() { return evenements; }
    public void setEvenements(List<Activite> evenements) { this.evenements = evenements; }
}
