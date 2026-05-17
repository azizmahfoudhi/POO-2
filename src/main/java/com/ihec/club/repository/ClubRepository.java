package com.ihec.club.repository;

import com.ihec.club.model.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository Spring Data JPA pour les clubs.
 */
public interface ClubRepository extends JpaRepository<Club, Long> {
    Optional<Club> findByNom(String nom);
}
