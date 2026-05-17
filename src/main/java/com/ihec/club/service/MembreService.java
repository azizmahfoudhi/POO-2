package com.ihec.club.service;

import com.ihec.club.model.*;
import com.ihec.club.repository.MembreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service métier pour la gestion des membres.
 * Contient la logique d'authentification et d'inscription.
 */
@Service
public class MembreService {

    @Autowired
    private MembreRepository membreRepository;

    /** Authentification par email et mot de passe */
    public Optional<Membre> authentifier(String email, String password) {
        return membreRepository.findByEmailAndPassword(email, password);
    }

    /** Inscription d'un nouvel étudiant */
    public Etudiant inscrire(String nom, String prenom, String email, String password) {
        if (!email.endsWith("@ihec.ucar.tn")) {
            throw new IllegalArgumentException("L'email doit se terminer par @ihec.ucar.tn");
        }
        if (membreRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email déjà utilisé !");
        }
        Etudiant etudiant = new Etudiant(nom, prenom, email, password);
        return membreRepository.save(etudiant);
    }

    /** CRUD : Lire tous les membres */
    public List<Membre> getTousLesMembres() {
        return membreRepository.findAll();
    }

    /** CRUD : Lire un membre par ID */
    public Optional<Membre> getMembreById(Long id) {
        return membreRepository.findById(id);
    }

    /** CRUD : Lire un membre par email */
    public Optional<Membre> getMembreByEmail(String email) {
        return membreRepository.findByEmail(email);
    }

    /** CRUD : Mettre à jour un membre */
    public Membre updateMembre(Long id, Membre details) {
        Membre membre = membreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Membre non trouvé"));
        membre.setNom(details.getNom());
        membre.setPrenom(details.getPrenom());
        membre.setEmail(details.getEmail());
        if (details.getPassword() != null && !details.getPassword().isEmpty()) {
            membre.setPassword(details.getPassword());
        }
        return membreRepository.save(membre);
    }

    /** CRUD : Supprimer un membre */
    public void deleteMembre(Long id) {
        membreRepository.deleteById(id);
    }

    /** Sauvegarder un membre (création) */
    public Membre saveMembre(Membre membre) {
        return membreRepository.save(membre);
    }
}
