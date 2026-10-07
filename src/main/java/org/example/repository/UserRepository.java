package org.example.repository;

import org.example.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByEmail(String email);

    /** Just the token version - cheap, used on every authenticated request (cached). */
    @Query("select u.tokenVersion from UserEntity u where u.username = :username")
    Optional<Integer> findTokenVersionByUsername(@Param("username") String username);

    /** Invalidates all JWTs of this user. */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update UserEntity u set u.tokenVersion = u.tokenVersion + 1 where u.username = :username")
    int incrementTokenVersion(@Param("username") String username);
}
