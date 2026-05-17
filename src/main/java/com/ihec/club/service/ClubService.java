package com.ihec.club.service;

import com.ihec.club.model.*;
import com.ihec.club.repository.ClubRepository;
import com.ihec.club.repository.MembreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service métier pour la gestion des clubs.
 * Contient la logique de création de club, gestion des membres, etc.
 */
@Service
public class ClubService {

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private MembreRepository membreRepository;

    /** CRUD : Lire tous les clubs */
    public List<Club> getTousLesClubs() {
        return clubRepository.findAll();
    }

    /** CRUD : Lire un club par ID */
    public Optional<Club> getClubById(Long id) {
        return clubRepository.findById(id);
    }

    /** CRUD : Créer un club */
    @Transactional
    public Club creerClub(String nom, String type, double budgetInitial) {
        Budget budget = new Budget(budgetInitial);
        Club club = new Club(nom, type, budget);
        return clubRepository.save(club);
    }

    /** CRUD : Mettre à jour un club */
    @Transactional
    public Club updateClub(Long id, String nom, String type) {
        Club club = clubRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Club non trouvé"));
        club.setNom(nom);
        club.setType(type);
        return clubRepository.save(club);
    }

    /** CRUD : Supprimer un club */
    @Transactional
    public void deleteClub(Long id) {
        clubRepository.deleteById(id);
    }

    /** Ajouter un membre à un club */
    @Transactional
    public Club ajouterMembre(Long clubId, Long membreId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new RuntimeException("Club non trouvé"));
        Membre membre = membreRepository.findById(membreId)
                .orElseThrow(() -> new RuntimeException("Membre non trouvé"));
        club.ajouterMembre(membre);
        return clubRepository.save(club);
    }

    /** Supprimer un membre d'un club */
    @Transactional
    public Club supprimerMembre(Long clubId, Long membreId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new RuntimeException("Club non trouvé"));
        Membre membre = membreRepository.findById(membreId)
                .orElseThrow(() -> new RuntimeException("Membre non trouvé"));
        club.supprimerMembre(membre.getEmail());
        return clubRepository.save(club);
    }

    /** Obtenir les membres d'un club */
    public List<Membre> getMembres(Long clubId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new RuntimeException("Club non trouvé"));
        return club.getMembres();
    }

    /** Sauvegarder un club */
    public Club saveClub(Club club) {
        return clubRepository.save(club);
    }
}
