package com.backend.escort.repository;

import com.backend.escort.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review,Long> {
    List<Review> findByDriverId(Long driverId);
    List<Review> findByOrgId(Long orgId);
    List<Review> findByStudentId(Long studentId);
}
