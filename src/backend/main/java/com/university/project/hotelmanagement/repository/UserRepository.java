package com.university.project.hotelmanagement.repository;


import com.university.project.hotelmanagement.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity , Long> {

    Optional<UserEntity> findByEmail(String email);
    boolean existsByEmail(String email);

    @Modifying
    @Query("""
            DELETE FROM UserEntity u
            WHERE u.enabled = false
            AND (u.createdAt IS NULL OR u.createdAt < :cutoff)
            """)
    void deleteStaleUnverifiedUsers(@Param("cutoff") LocalDateTime cutoff);
}
