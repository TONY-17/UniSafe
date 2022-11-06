package com.backend.escort.repository;

import com.backend.escort.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface StudentRepository extends JpaRepository<Student,Long> {
    List<Student> findByOrganisationId(Long id);
    Student findByUserId(Long userId);

}
