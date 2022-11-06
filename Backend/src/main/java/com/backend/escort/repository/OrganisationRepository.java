package com.backend.escort.repository;

import com.backend.escort.model.Organisation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Repository
public interface OrganisationRepository extends JpaRepository<Organisation,Long> {
    List<Organisation> findByActive(boolean active);
    List<Organisation> findByNameContaining(String name);
    Boolean existsByDomain(String domain);
    Organisation findByDomain(String domain);
    Organisation findByName(String name);
    // Returns all the organisations of a Admin specified by id
    List<Organisation> findByUserId(Long id);

    // Search for organisations by name or domain
    @Query("SELECT o FROM Organisation o WHERE " +
            "o.name LIKE CONCAT('%',:query,'%')" +
            "or o.domain LIKE CONCAT('%',:query,'%')")
    List<Organisation> searchByName(String query);

}
