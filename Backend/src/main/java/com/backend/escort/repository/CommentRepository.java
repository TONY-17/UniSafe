package com.backend.escort.repository;

import com.backend.escort.model.Comment;
import com.backend.escort.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository

public interface CommentRepository extends JpaRepository<Comment,Long> {
    Optional<Student> findByStudentId(Long id);
    List<Comment> findByAlertId(Long id);
    @Transactional
    void deleteByAlertId(long id);

}
