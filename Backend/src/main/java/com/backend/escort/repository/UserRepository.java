package com.backend.escort.repository;

import com.backend.escort.model.Driver;
import com.backend.escort.model.Role;
import com.backend.escort.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findById(Long id);
    // Get user based on the email
    Optional<User> findByEmail(String email);
    // Checks if the email already exists
    Boolean existsByEmail(String email);
    // Retrieve a list of drivers
    List<Driver> findByRole(Role role);
}
