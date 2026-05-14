package com.university.project.hotelmanagement.repository;

import com.university.project.hotelmanagement.entity.Booking;
import com.university.project.hotelmanagement.entity.UserEntity;
import com.university.project.hotelmanagement.enums.BookingStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUser(UserEntity user);

    @Query("SELECT b FROM Booking b JOIN FETCH b.room r JOIN FETCH r.hotel h WHERE b.user.id = :userId")
    List<Booking> findAllBookingsByUserId(@Param("userId") Long userId);

    @Query("SELECT b FROM Booking b JOIN FETCH b.room r JOIN FETCH r.hotel h")
    List<Booking> findAllDetailedBookings();

    @Query("SELECT b FROM Booking b JOIN FETCH b.user JOIN FETCH b.room WHERE b.id = :id")
    Optional<Booking> findByBookingIdWithDetails(@Param("id") Long id);

    List<Booking> findAllByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM Booking b WHERE b.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.room.id = :roomId " +
            "AND (:checkIn < b.checkOutDate AND :checkOut > b.checkInDate)")
    boolean existsByRoomIdAndDateRange(Long roomId, LocalDate checkIn, LocalDate checkOut);


    @Query("SELECT COUNT(b) FROM Booking b " +
            "WHERE b.hotel.id = :hotelId " +
            "AND b.status = 'CONFIRMED' " +
            "AND NOT (b.checkOutDate <= :checkInDate OR b.checkInDate >= :checkOutDate)")
    long countOverlappingBookings(Long hotelId, LocalDate checkInDate, LocalDate checkOutDate);


    List<Booking> findByStatusAndExpiryTimeBefore(BookingStatus bookingStatus, LocalDateTime now);

    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.room.id = :roomId " +
            "AND b.status != 'CANCELLED' " +
            "AND :checkIn < b.checkOutDate AND :checkOut > b.checkInDate")
    boolean existsOverlappingBooking(
            @Param("roomId") Long roomId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );

    @Query("""
            SELECT COUNT(b) FROM Booking b
            WHERE b.room.id = :roomId
            AND :checkIn < b.checkOutDate
            AND :checkOut > b.checkInDate
            AND (
                b.status = 'CONFIRMED'
                OR (b.status = 'PENDING' AND (b.expiryTime IS NULL OR b.expiryTime > CURRENT_TIMESTAMP))
            )
            """)
    long countActiveOverlappingBookings(
            @Param("roomId") Long roomId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );
}
