package com.ihec.club.controller;

import com.ihec.club.model.*;
import com.ihec.club.service.EvenementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Contrôleur REST pour la gestion des événements.
 * Endpoints CRUD + gestion des propositions
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class EvenementController {

    @Autowired
    private EvenementService evenementService;

    /** GET /api/evenements — Tous les événements approuvés (vue étudiant) */
    @GetMapping("/evenements")
    public List<Activite> getAll() {
        return evenementService.getTousLesEvenements();
    }

    /** GET /api/evenements/{id} — Lire un événement */
    @GetMapping("/evenements/{id}")
    public ResponseEntity<Activite> getById(@PathVariable Long id) {
        return evenementService.getEvenementById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** GET /api/clubs/{clubId}/evenements — Événements approuvés d'un club */
    @GetMapping("/clubs/{clubId}/evenements")
    public List<Activite> getByClub(@PathVariable Long clubId) {
        return evenementService.getEvenementsClub(clubId);
    }

    /** POST /api/clubs/{clubId}/evenements — Créer un événement */
    @PostMapping("/clubs/{clubId}/evenements")
    public ResponseEntity<?> create(@PathVariable Long clubId, @RequestBody Map<String, Object> data) {
        try {
            String type = (String) data.get("type");
            String titre = (String) data.get("titre");
            String date = (String) data.get("date");
            int duree = Integer.parseInt(data.get("duree").toString());
            String lieu = (String) data.get("lieu");

            Activite evenement;
            switch (type) {
                case "academique" -> {
                    double cout = Double.parseDouble(data.get("coutGlobal").toString());
                    evenement = new EvenementAcademique(titre, date, duree, lieu, cout);
                }
                case "sportif" -> {
                    double coutEquip = Double.parseDouble(data.get("coutEquip").toString());
                    double coutLog = Double.parseDouble(data.getOrDefault("coutLog", "0").toString());
                    evenement = new EvenementSportif(titre, date, duree, lieu, coutEquip, coutLog);
                }
                case "culturel" -> {
                    double coutDeco = Double.parseDouble(data.get("coutDeco").toString());
                    double coutAudio = Double.parseDouble(data.getOrDefault("coutAudio", "0").toString());
                    evenement = new EvenementCulturel(titre, date, duree, lieu, coutDeco, coutAudio);
                }
                default -> {
                    return ResponseEntity.badRequest().body(Map.of("error", "Type invalide"));
                }
            }

            Activite saved = evenementService.ajouterEvenement(clubId, evenement);
            return ResponseEntity.ok(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** PUT /api/evenements/{id} — Mettre à jour un événement */
    @PutMapping("/evenements/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Activite details) {
        try {
            return ResponseEntity.ok(evenementService.updateEvenement(id, details));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** DELETE /api/evenements/{id} — Supprimer un événement */
    @DeleteMapping("/evenements/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        evenementService.deleteEvenement(id);
        return ResponseEntity.ok().build();
    }

    // ===== Propositions =====

    /** GET /api/clubs/{clubId}/propositions — Propositions en attente */
    @GetMapping("/clubs/{clubId}/propositions")
    public List<Activite> getPropositions(@PathVariable Long clubId) {
        return evenementService.getPropositionsClub(clubId);
    }

    /** POST /api/clubs/{clubId}/propositions — Proposer un événement */
    @PostMapping("/clubs/{clubId}/propositions")
    public ResponseEntity<?> proposer(@PathVariable Long clubId, @RequestBody Map<String, Object> data) {
        try {
            Long membreId = Long.parseLong(data.get("membreId").toString());
            String titre = (String) data.get("titre");
            String date = (String) data.get("date");
            int duree = Integer.parseInt(data.get("duree").toString());
            String lieu = (String) data.get("lieu");
            String explication = (String) data.get("explication");

            EvenementProposition prop = new EvenementProposition(titre, date, duree, lieu, explication);
            Activite saved = evenementService.proposerEvenement(clubId, membreId, prop);
            return ResponseEntity.ok(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** PUT /api/propositions/{id}/accepter — Accepter une proposition */
    @PutMapping("/propositions/{id}/accepter")
    public ResponseEntity<?> accepter(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(evenementService.accepterProposition(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** PUT /api/propositions/{id}/refuser — Refuser une proposition */
    @PutMapping("/propositions/{id}/refuser")
    public ResponseEntity<Void> refuser(@PathVariable Long id) {
        evenementService.refuserProposition(id);
        return ResponseEntity.ok().build();
    }
}
