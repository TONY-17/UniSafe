package com.backend.escort.repository;

import com.backend.escort.model.Emergency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public interface EmergencyRepository  extends JpaRepository<Emergency,Long> {
    List<Emergency> findByOrgId(Long orgId);
    List<Emergency> findByStudentId(Long studentId);
}
