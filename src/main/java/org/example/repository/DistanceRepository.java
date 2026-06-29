package org.example.repository;
import org.example.model.Distance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DistanceRepository extends JpaRepository<Distance, UUID> {
    List<Distance> findAllByOwnerId(String ownerId);
}

