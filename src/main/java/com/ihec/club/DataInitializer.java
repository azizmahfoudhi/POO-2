package com.ihec.club;

import com.ihec.club.model.*;
import com.ihec.club.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Initialise les données de démonstration au démarrage de l'application.
 * Ne s'exécute que si la base de données est vide.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired private ClubRepository clubRepository;
    @Autowired private MembreRepository membreRepository;
    @Autowired private ActiviteRepository activiteRepository;

    @Override
    public void run(String... args) {
        // Ne pas réinitialiser si des données existent déjà
        if (membreRepository.count() > 0) {
            System.out.println("Base de données déjà initialisée.");
            return;
        }

        System.out.println("=== Initialisation des données de démonstration ===");

        // ===== Admin système =====
        Admin admin = new Admin("Admin", "Systeme", "admin@ihec.ucar.tn", "admin123");
        membreRepository.save(admin);

        // ===== Présidents =====
        President presEnactus = new President("Ali", "Ben Salah", "ali@ihec.ucar.tn", "ali123");
        President presLions = new President("Sara", "Ben Ali", "sara@ihec.ucar.tn", "sara123");
        President presArt = new President("Leila", "Ben Youssef", "leila@ihec.ucar.tn", "leila123");
        membreRepository.save(presEnactus);
        membreRepository.save(presLions);
        membreRepository.save(presArt);

        // ===== Clubs =====
        Club clubEnactus = new Club("Enactus", "Entrepreneuriat", new Budget(10000));
        Club clubLions = new Club("Lions Club", "Service", new Budget(8000));
        Club clubArt = new Club("Art Revolution", "Culture", new Budget(6000));

        clubEnactus.ajouterMembre(presEnactus);
        clubLions.ajouterMembre(presLions);
        clubArt.ajouterMembre(presArt);

        // ===== Étudiants (10 par club) =====
        for (int i = 1; i <= 10; i++) {
            Etudiant e1 = new Etudiant("EtudiantEnactus" + i, "Nom" + i, "enactus" + i + "@ihec.ucar.tn", "pass" + i);
            Etudiant e2 = new Etudiant("EtudiantLions" + i, "Nom" + i, "lions" + i + "@ihec.ucar.tn", "pass" + i);
            Etudiant e3 = new Etudiant("EtudiantArt" + i, "Nom" + i, "art" + i + "@ihec.ucar.tn", "pass" + i);
            membreRepository.save(e1);
            membreRepository.save(e2);
            membreRepository.save(e3);
            clubEnactus.ajouterMembre(e1);
            clubLions.ajouterMembre(e2);
            clubArt.ajouterMembre(e3);
        }

        clubRepository.save(clubEnactus);
        clubRepository.save(clubLions);
        clubRepository.save(clubArt);

        // ===== Événements initiaux =====
        EvenementAcademique conf = new EvenementAcademique("Conf IA", "15/12/2025", 3, "Amphi A", 500);
        conf.setClub(clubEnactus);
        conf.setStatut("APPROUVE");
        clubEnactus.getBudget().ajouterDepense(500);

        EvenementCulturel concert = new EvenementCulturel("Concert", "20/12/2025", 2, "Salle C", 550, 0);
        concert.setClub(clubArt);
        concert.setStatut("APPROUVE");
        clubArt.getBudget().ajouterDepense(550);

        activiteRepository.save(conf);
        activiteRepository.save(concert);
        clubRepository.save(clubEnactus);
        clubRepository.save(clubArt);

        System.out.println("=== Données initialisées avec succès ===");
        System.out.println("Admin: admin@ihec.ucar.tn / admin123");
        System.out.println("Presidents: ali@ / ali123, sara@ / sara123, leila@ / leila123");
    }
}
