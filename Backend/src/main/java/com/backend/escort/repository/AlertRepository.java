package com.backend.escort.repository;

import com.backend.escort.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert,Long> {
        List<Alert> findByReported(boolean reported);
        List<Alert> findByTag(String tag);
        List<Alert> findByOrgId(Long orgId);
        boolean existsById(Long id);
}
