package com.ihec.club.repository;

import com.ihec.club.model.Activite;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repository Spring Data JPA pour les activités/événements.
 */
public interface ActiviteRepository extends JpaRepository<Activite, Long> {
    List<Activite> findByClubId(Long clubId);
    List<Activite> findByClubIdAndStatut(Long clubId, String statut);
    List<Activite> findByStatut(String statut);
}
