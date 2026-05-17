package com.ihec.club.controller;

import com.ihec.club.model.Club;
import com.ihec.club.model.Membre;
import com.ihec.club.service.ClubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Contrôleur REST pour la gestion des clubs.
 * Endpoints CRUD : GET, POST, PUT, DELETE
 */
@RestController
@RequestMapping("/api/clubs")
@CrossOrigin(origins = "*")
public class ClubController {

    @Autowired
    private ClubService clubService;

    /** GET /api/clubs — Lire tous les clubs */
    @GetMapping
    public List<Club> getAll() {
        return clubService.getTousLesClubs();
    }

    /** GET /api/clubs/{id} — Lire un club avec ses membres et événements */
    @GetMapping("/{id}")
    public ResponseEntity<Club> getById(@PathVariable Long id) {
        return clubService.getClubById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** POST /api/clubs — Créer un club */
    @PostMapping
    public ResponseEntity<Club> create(@RequestBody Map<String, Object> data) {
        String nom = (String) data.get("nom");
        String type = (String) data.get("type");
        double budget = Double.parseDouble(data.get("budget").toString());
        Club club = clubService.creerClub(nom, type, budget);
        return ResponseEntity.ok(club);
    }

    /** PUT /api/clubs/{id} — Mettre à jour un club */
    @PutMapping("/{id}")
    public ResponseEntity<Club> update(@PathVariable Long id, @RequestBody Map<String, String> data) {
        try {
            Club club = clubService.updateClub(id, data.get("nom"), data.get("type"));
            return ResponseEntity.ok(club);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** DELETE /api/clubs/{id} — Supprimer un club */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clubService.deleteClub(id);
        return ResponseEntity.ok().build();
    }

    // ===== Gestion des membres du club =====

    /** GET /api/clubs/{id}/membres — Lire les membres d'un club */
    @GetMapping("/{id}/membres")
    public ResponseEntity<List<Membre>> getMembres(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(clubService.getMembres(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** POST /api/clubs/{id}/membres/{membreId} — Ajouter un membre au club */
    @PostMapping("/{id}/membres/{membreId}")
    public ResponseEntity<Club> ajouterMembre(@PathVariable Long id, @PathVariable Long membreId) {
        try {
            return ResponseEntity.ok(clubService.ajouterMembre(id, membreId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /** DELETE /api/clubs/{id}/membres/{membreId} — Retirer un membre du club */
    @DeleteMapping("/{id}/membres/{membreId}")
    public ResponseEntity<Club> supprimerMembre(@PathVariable Long id, @PathVariable Long membreId) {
        try {
            return ResponseEntity.ok(clubService.supprimerMembre(id, membreId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}
