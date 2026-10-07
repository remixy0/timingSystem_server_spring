package org.example.repository;
import org.example.model.Athlete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AthleteRepository extends JpaRepository<Athlete, UUID> {
    List<Athlete> findAllByOwnerId(String ownerId);

    /** Just the name columns – doesn't load the (large) photo. */
    interface AthleteName {
        UUID getId();
        String getName();
        String getSurname();
    }

    @Query("select a.id as id, a.name as name, a.surname as surname from Athlete a where a.id in :ids")
    List<AthleteName> findNamesByIdIn(@Param("ids") Collection<UUID> ids);

    Optional<Athlete> findByIdAndOwnerId(UUID id, String ownerId);

    boolean existsByIdAndOwnerId(UUID id, String ownerId);
}
