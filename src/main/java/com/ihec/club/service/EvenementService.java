package com.ihec.club.service;

import com.ihec.club.model.*;
import com.ihec.club.repository.ActiviteRepository;
import com.ihec.club.repository.ClubRepository;
import com.ihec.club.repository.MembreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service métier pour la gestion des événements.
 * Gère la création, les propositions, et la validation budgétaire.
 */
@Service
public class EvenementService {

    @Autowired
    private ActiviteRepository activiteRepository;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private MembreRepository membreRepository;

    /** CRUD : Lire tous les événements approuvés */
    public List<Activite> getTousLesEvenements() {
        return activiteRepository.findByStatut("APPROUVE");
    }

    /** CRUD : Lire un événement par ID */
    public Optional<Activite> getEvenementById(Long id) {
        return activiteRepository.findById(id);
    }

    /** Lire les événements approuvés d'un club */
    public List<Activite> getEvenementsClub(Long clubId) {
        return activiteRepository.findByClubIdAndStatut(clubId, "APPROUVE");
    }

    /** Lire les propositions en attente d'un club */
    public List<Activite> getPropositionsClub(Long clubId) {
        return activiteRepository.findByClubIdAndStatut(clubId, "EN_ATTENTE");
    }

    /** CRUD : Créer un événement (avec vérification du budget) */
    @Transactional
    public Activite ajouterEvenement(Long clubId, Activite evenement) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new RuntimeException("Club non trouvé"));

        double coutTotal = evenement.getCoutTotal();
        Budget budget = club.getBudget();

        if (!budget.aSuffisammentDeFonds(coutTotal)) {
            throw new RuntimeException("Budget insuffisant ! Solde: " + budget.getSolde()
                    + " | Coût: " + coutTotal);
        }

        budget.ajouterDepense(coutTotal);
        evenement.setClub(club);
        evenement.setStatut("APPROUVE");
        return activiteRepository.save(evenement);
    }

    /** Proposer un événement (par un étudiant) */
    @Transactional
    public Activite proposerEvenement(Long clubId, Long membreId, EvenementProposition proposition) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new RuntimeException("Club non trouvé"));
        Membre proposeur = membreRepository.findById(membreId)
                .orElseThrow(() -> new RuntimeException("Membre non trouvé"));

        proposition.setClub(club);
        proposition.setProposeur(proposeur);
        proposition.setStatut("EN_ATTENTE");
        return activiteRepository.save(proposition);
    }

    /** Accepter une proposition */
    @Transactional
    public Activite accepterProposition(Long propositionId) {
        Activite proposition = activiteRepository.findById(propositionId)
                .orElseThrow(() -> new RuntimeException("Proposition non trouvée"));

        double coutTotal = proposition.getCoutTotal();
        Budget budget = proposition.getClub().getBudget();

        if (coutTotal > 0 && !budget.aSuffisammentDeFonds(coutTotal)) {
            throw new RuntimeException("Budget insuffisant !");
        }

        if (coutTotal > 0) {
            budget.ajouterDepense(coutTotal);
        }
        proposition.setStatut("APPROUVE");
        return activiteRepository.save(proposition);
    }

    /** Refuser une proposition */
    @Transactional
    public void refuserProposition(Long propositionId) {
        Activite proposition = activiteRepository.findById(propositionId)
                .orElseThrow(() -> new RuntimeException("Proposition non trouvée"));
        proposition.setStatut("REFUSE");
        activiteRepository.save(proposition);
    }

    /** CRUD : Mettre à jour un événement */
    @Transactional
    public Activite updateEvenement(Long id, Activite details) {
        Activite evenement = activiteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Événement non trouvé"));
        evenement.setTitre(details.getTitre());
        evenement.setDate(details.getDate());
        evenement.setDuree(details.getDuree());
        evenement.setLieu(details.getLieu());
        return activiteRepository.save(evenement);
    }

    /** CRUD : Supprimer un événement */
    @Transactional
    public void deleteEvenement(Long id) {
        activiteRepository.deleteById(id);
    }
}
