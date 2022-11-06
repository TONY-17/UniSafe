package com.backend.escort.repository;

import com.backend.escort.model.Driver;
import com.backend.escort.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver,Long> {
    // Retrieves all the drivers under a certain organisation
    List<Driver> findByOrganisationId(Long id);

    // Retrieves all the drivers that are registered by an admi
    List<Driver> findByAdminId(Long adminId);

    // Allows the admin to search for drivers, the search filters by name or last name
    @Query("SELECT d FROM Driver d WHERE " +
            "d.firstName LIKE CONCAT('%',:query,'%')" +
            "or d.lastName LIKE CONCAT('%',:query,'%')"
    )
    List<Driver> searchDrivers(String query);

    Optional<Driver> findByUserId(Long userId);

}
