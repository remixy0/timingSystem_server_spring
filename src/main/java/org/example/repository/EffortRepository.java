package org.example.repository;
import org.example.model.Effort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface EffortRepository extends JpaRepository<Effort, UUID> {}

