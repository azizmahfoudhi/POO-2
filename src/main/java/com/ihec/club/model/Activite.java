package com.ihec.club.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

/**
 * Classe abstraite pour tous les événements / activités.
 * Utilise l'héritage SINGLE_TABLE avec discriminateur "type_evenement".
 *
 * Démontre : Abstraction (classe abstraite), Polymorphisme (getCoutTotal)
 */
@Entity
@Table(name = "activite")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type_evenement")
public abstract class Activite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;
    private String date;
    private int duree;
    private String lieu;

    /** Statut : APPROUVE ou EN_ATTENTE */
    @Column(nullable = false)
    private String statut = "APPROUVE";

    /** Club organisateur */
    @ManyToOne
    @JoinColumn(name = "club_id")
    @JsonIgnore
    private Club club;

    /** Membre ayant proposé l'événement (nullable si créé par admin/président) */
    @ManyToOne
    @JoinColumn(name = "proposeur_id")
    private Membre proposeur;

    /** Colonne discriminateur en lecture seule */
    @Column(name = "type_evenement", insertable = false, updatable = false)
    private String typeEvenement;

    // ===== Constructeurs =====
    protected Activite() {}

    public Activite(String titre, String date, int duree, String lieu) {
        if (!date.matches("\\d{2}/\\d{2}/\\d{4}"))
            throw new IllegalArgumentException("Date invalide, utilisez JJ/MM/AAAA");
        this.titre = titre;
        this.date = date;
        this.duree = duree;
        this.lieu = lieu;
    }

    // ===== Méthode abstraite — Polymorphisme =====

    /**
     * Calcule le coût total de l'événement.
     * Chaque sous-classe implémente sa propre logique de calcul.
     */
    public abstract double getCoutTotal();

    // ===== Getters & Setters =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public int getDuree() { return duree; }
    public void setDuree(int duree) { this.duree = duree; }

    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public Club getClub() { return club; }
    public void setClub(Club club) { this.club = club; }

    public Membre getProposeur() { return proposeur; }
    public void setProposeur(Membre proposeur) { this.proposeur = proposeur; }

    public String getTypeEvenement() { return typeEvenement; }

    @Override
    public String toString() {
        return titre + " - " + date + " (" + duree + "h) | Lieu: " + lieu;
    }
}
