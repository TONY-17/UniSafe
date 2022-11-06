package com.backend.escort.repository;

import com.backend.escort.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ImagesRepository extends JpaRepository<Image,String> {
    List<Image> findByUniqueAlertId(String uniqueAlertId);
    boolean existsByUniqueAlertId(String uniqueAlertId);

}
