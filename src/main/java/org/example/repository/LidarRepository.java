package org.example.repository;

import org.example.model.LidarData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LidarRepository extends JpaRepository<LidarData, UUID> {

    Optional<LidarData> findByEffortIdAndOwnerId(UUID effortId, String ownerId);

    /** Just the ids – doesn't load the (large) sample lists. */
    @Query("select l.effortId from LidarData l where l.ownerId = :ownerId")
    List<UUID> findEffortIdsByOwnerId(@Param("ownerId") String ownerId);
}
