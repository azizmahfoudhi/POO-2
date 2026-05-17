package com.ihec.club.controller;

import com.ihec.club.model.Membre;
import com.ihec.club.service.MembreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Contrôleur REST pour l'authentification et la gestion des membres.
 * Endpoints CRUD : GET, POST, PUT, DELETE
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class MembreController {

    @Autowired
    private MembreService membreService;

    // ===== Authentification =====

    /** POST /api/auth/login — Connexion */
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");
        return membreService.authentifier(email, password)
                .map(m -> ResponseEntity.ok(m))
                .orElse(ResponseEntity.status(401).build());
    }

    /** POST /api/auth/register — Inscription d'un étudiant */
    @PostMapping("/auth/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> data) {
        try {
            Membre m = membreService.inscrire(
                    data.get("nom"), data.get("prenom"),
                    data.get("email"), data.get("password"));
            return ResponseEntity.ok(m);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ===== CRUD Membres =====

    /** GET /api/membres — Lire tous les membres */
    @GetMapping("/membres")
    public List<Membre> getAll() {
        return membreService.getTousLesMembres();
    }

    /** GET /api/membres/{id} — Lire un membre */
    @GetMapping("/membres/{id}")
    public ResponseEntity<Membre> getById(@PathVariable Long id) {
        return membreService.getMembreById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** PUT /api/membres/{id} — Mettre à jour un membre */
    @PutMapping("/membres/{id}")
    public ResponseEntity<Membre> update(@PathVariable Long id, @RequestBody Membre details) {
        try {
            return ResponseEntity.ok(membreService.updateMembre(id, details));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** DELETE /api/membres/{id} — Supprimer un membre */
    @DeleteMapping("/membres/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        membreService.deleteMembre(id);
        return ResponseEntity.ok().build();
    }
}
