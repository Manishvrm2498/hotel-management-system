package com.university.project.hotelmanagement.repository;

import com.university.project.hotelmanagement.entity.RoomLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RoomLockRepository extends JpaRepository<RoomLock, Long> {

    Optional<RoomLock> findByRoomIdAndLockExpiryAfter(Long roomId, LocalDateTime now);

    void deleteByRoomId(Long roomId);

}