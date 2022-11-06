package com.backend.escort.repository;

import com.backend.escort.model.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import javax.transaction.Transactional;

import java.util.List;


@Repository
@Transactional
public interface TripRepository extends JpaRepository<Trip,Long> {
    //List<Trip> findAll();
    List<Trip> findByDriverId(Long id);
    List<Trip> findByUserId(Long id);
    List<Trip> findByAccepted(boolean accepted);

    List<Trip> findByDateCreatedContaining(String dateCreated);

    @Query("SELECT t FROM Trip t WHERE " +
            "t.pickUp LIKE CONCAT('%',:query,'%')" +
            "or t.destination LIKE CONCAT('%',:query,'%')")
    List<Trip> searchTrips(String query);

    // Retrieve all the trips under an organisation
    List<Trip> findByOrgId(Long id);


}
