package com.backend.escort.repository;

import com.backend.escort.model.DriverImages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DriverImageRepository extends JpaRepository<DriverImages,String> {
    boolean existsByDriverId(Long driverId);
    DriverImages findByDriverId(Long driverId);
}
