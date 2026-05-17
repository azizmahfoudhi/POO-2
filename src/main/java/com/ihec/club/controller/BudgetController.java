package com.ihec.club.controller;

import com.ihec.club.model.Budget;
import com.ihec.club.model.Club;
import com.ihec.club.service.ClubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Contrôleur REST pour la gestion des budgets.
 * Endpoints : GET et PUT
 */
@RestController
@RequestMapping("/api/clubs/{clubId}/budget")
@CrossOrigin(origins = "*")
public class BudgetController {

    @Autowired
    private ClubService clubService;

    /** GET /api/clubs/{clubId}/budget — Voir le solde */
    @GetMapping
    public ResponseEntity<Budget> getBudget(@PathVariable Long clubId) {
        return clubService.getClubById(clubId)
                .map(club -> ResponseEntity.ok(club.getBudget()))
                .orElse(ResponseEntity.notFound().build());
    }

    /** PUT /api/clubs/{clubId}/budget — Définir le budget */
    @PutMapping
    public ResponseEntity<?> setBudget(@PathVariable Long clubId, @RequestBody Map<String, Double> data) {
        try {
            Club club = clubService.getClubById(clubId)
                    .orElseThrow(() -> new RuntimeException("Club non trouvé"));
            double nouveauSolde = data.get("solde");
            if (nouveauSolde < 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Le solde ne peut pas être négatif"));
            }
            club.getBudget().setSolde(nouveauSolde);
            clubService.saveClub(club);
            return ResponseEntity.ok(club.getBudget());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
