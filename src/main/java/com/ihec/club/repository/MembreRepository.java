package com.ihec.club.repository;

import com.ihec.club.model.Membre;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository Spring Data JPA pour les membres.
 * Les méthodes CRUD sont générées automatiquement par Spring.
 */
public interface MembreRepository extends JpaRepository<Membre, Long> {
    Optional<Membre> findByEmail(String email);
    Optional<Membre> findByEmailAndPassword(String email, String password);
    boolean existsByEmail(String email);
}
