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

    @Query("""
            SELECT DISTINCT b FROM Booking b
            JOIN FETCH b.room r
            JOIN FETCH r.hotel h
            LEFT JOIN FETCH b.user u
            WHERE b.user.id = :userId
               OR LOWER(b.email) = LOWER(:email)
               OR (
                    LOWER(b.firstName) = LOWER(:firstName)
                    AND LOWER(b.lastName) = LOWER(:lastName)
               )
            """)
    List<Booking> findAiBookingsForUser(
            @Param("userId") Long userId,
            @Param("email") String email,
            @Param("firstName") String firstName,
            @Param("lastName") String lastName
    );

    @Query("SELECT b FROM Booking b JOIN FETCH b.room r JOIN FETCH r.hotel h")
    List<Booking> findAllDetailedBookings();

    @Query("SELECT b FROM Booking b JOIN FETCH b.user JOIN FETCH b.room WHERE b.id = :id")
    Optional<Booking> findByBookingIdWithDetails(@Param("id") Long id);

    @Query(value = """
            SELECT
                b.id AS id,
                b.firstName AS firstName,
                b.lastName AS lastName,
                COALESCE(b.email, u.email) AS email,
                b.phoneNumber AS phoneNumber,
                b.totalGuests AS totalGuests,
                b.checkInDate AS checkInDate,
                b.checkOutDate AS checkOutDate,
                b.totalPrice AS totalPrice,
                b.status AS status,
                b.bookedAt AS bookedAt,
                b.user_id AS userId,
                u.email AS userEmail,
                h.name AS hotelName,
                r.type AS roomType,
                admin.email AS adminEmail
            FROM booking b
            JOIN hotel h ON b.hotel_id = h.id
            JOIN room r ON b.room_id = r.id
            LEFT JOIN users u ON b.user_id = u.id
            LEFT JOIN users admin ON h.admin_id = admin.id
            WHERE b.id = :id
            """, nativeQuery = true)
    Optional<AdminBookingRow> findAdminBookingRowById(@Param("id") Long id);

    interface AdminBookingRow {
        Long getId();
        String getFirstName();
        String getLastName();
        String getEmail();
        String getPhoneNumber();
        Integer getTotalGuests();
        LocalDate getCheckInDate();
        LocalDate getCheckOutDate();
        Double getTotalPrice();
        String getStatus();
        LocalDateTime getBookedAt();
        Long getUserId();
        String getUserEmail();
        String getHotelName();
        String getRoomType();
        String getAdminEmail();
    }

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
