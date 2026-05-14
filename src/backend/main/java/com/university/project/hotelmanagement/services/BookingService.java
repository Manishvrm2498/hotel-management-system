package com.university.project.hotelmanagement.services;

import com.university.project.hotelmanagement.dto.BookingRequestDTO;
import com.university.project.hotelmanagement.dto.BookingResponseDTO;
import com.university.project.hotelmanagement.dto.UserBookingDetailsDTO;
import com.university.project.hotelmanagement.entity.*;
import com.university.project.hotelmanagement.enums.BookingStatus;
import com.university.project.hotelmanagement.exception.BadRequestException;
import com.university.project.hotelmanagement.exception.DuplicateResourceException;
import com.university.project.hotelmanagement.exception.ResourceNotFoundException;
import com.university.project.hotelmanagement.repository.BookingRepository;
import com.university.project.hotelmanagement.repository.RoomLockRepository;
import com.university.project.hotelmanagement.repository.RoomRepository;
import com.university.project.hotelmanagement.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final RoomLockRepository roomLockRepository;

    public BookingService(BookingRepository bookingRepository, RoomRepository roomRepository, UserRepository userRepository, RoomLockRepository roomLockRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.roomLockRepository = roomLockRepository;
    }

    private UserEntity getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("User not authenticated");
        }
        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found in database"));
    }


    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cancelExpiredPendingBookings() {

        List<Booking> expiredBookings =
                bookingRepository.findByStatusAndExpiryTimeBefore(
                        BookingStatus.PENDING,
                        LocalDateTime.now()
                );

        for (Booking booking : expiredBookings) {
            booking.setStatus(BookingStatus.EXPIRED);
        }
    }

    @Transactional
    public BookingResponseDTO createBooking(BookingRequestDTO request) {
        UserEntity currentUser = userRepository.findById(getCurrentUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        if (room.getHotel() == null || !room.getHotel().getId().equals(request.getHotelId())) {
            throw new BadRequestException("Room ID " + request.getRoomId() + " does not belong to Hotel ID " + request.getHotelId());
        }
        if (!room.isAvailable() || room.getTotalRooms() <= 0) {
            throw new BadRequestException("Room is not currently available.");
        }


        long days = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
        if (days <= 0) {
            throw new BadRequestException("Check-out date must be after check-in date");
        }

        long overlappingBookings = bookingRepository.countActiveOverlappingBookings(
                request.getRoomId(),
                request.getCheckInDate(),
                request.getCheckOutDate()
        );

        if (overlappingBookings >= room.getTotalRooms()) {
            throw new DuplicateResourceException("Room is already booked for these dates.");
        }

        Booking booking = new Booking();
        booking.setBookingReference("HTL-" + UUID.randomUUID().toString().toUpperCase().substring(0, 8));
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        booking.setRoom(room);
        booking.setHotel(room.getHotel());
        booking.setUser(currentUser);


        booking.setFirstName(request.getFirstName());
        booking.setLastName(request.getLastName());
        booking.setEmail(request.getEmail());
        booking.setPhoneNumber(request.getPhoneNumber());
        booking.setTotalGuests(request.getTotalGuests());

        booking.setTotalPrice(days * room.getPrice());

        booking.setStatus(BookingStatus.PENDING);
        booking.setBookedAt(LocalDateTime.now());
        booking.setExpiryTime(LocalDateTime.now().plusMinutes(15));

        Booking savedBooking = bookingRepository.save(booking);
        return mapToResponse(savedBooking);
    }
    public BookingResponseDTO getBookingByIdForAdmin(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        String currentAdminEmail = getCurrentUser().getEmail();
        if (booking.getHotel() == null
                || booking.getHotel().getAdmin() == null
                || !currentAdminEmail.equals(booking.getHotel().getAdmin().getEmail())) {
            throw new ResourceNotFoundException("Booking not found with ID: " + bookingId);
        }

        return mapToResponse(booking);
    }

    public List<BookingResponseDTO> getBookingsByUser() {
        UserEntity currentUser = getCurrentUser();
        return bookingRepository.findByUser(currentUser)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }



    @Transactional
    public String cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        UserEntity currentUser = getCurrentUser();
        if (!booking.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not authorized to cancel this booking!");
        }
        if (BookingStatus.CANCELLED.equals(booking.getStatus())) {
            return "Booking is already cancelled.";
        }
        if (booking.getCheckInDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Cannot cancel a past or ongoing booking!");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        return "Booking cancelled successfully for " + booking.getRoom().getHotel().getName();
    }


    @Transactional(readOnly = true)
    public List<UserBookingDetailsDTO> getUserFullDetails() {
        UserEntity currentUser = getCurrentUser();

        List<Booking> bookings = bookingRepository.findAllBookingsByUserId(currentUser.getId());

        return bookings.stream().map(b -> {
            UserBookingDetailsDTO dto = new UserBookingDetailsDTO();
            dto.setBookingId(b.getId());
            dto.setUserName(currentUser.getFirstName()+" "+currentUser.getLastName());

            Hotel hotel = b.getRoom().getHotel();
            dto.setHotelName(hotel.getName());
            dto.setState(hotel.getState());
            dto.setDistrict(hotel.getDistrict());

            dto.setRoomType(b.getRoom().getType().toString());
            dto.setCheckIn(b.getCheckInDate());
            dto.setCheckOut(b.getCheckOutDate());
            dto.setAmount(b.getTotalPrice());
            dto.setStatus(b.getStatus().name());

            return dto;
        }).collect(Collectors.toList());
    }


    private BookingResponseDTO mapToResponse(Booking b) {
        BookingResponseDTO dto = new BookingResponseDTO();

        dto.setId(b.getId());
        dto.setHotelName(b.getHotel().getName());
        dto.setRoomType(b.getRoom().getType().toString());
        dto.setCheckInDate(b.getCheckInDate());
        dto.setCheckOutDate(b.getCheckOutDate());
        dto.setTotalPrice(b.getTotalPrice());

        dto.setStatus(b.getStatus().name());

        dto.setBookedAt(b.getBookedAt());

        if (b.getUser() != null) {
            dto.setUsername(b.getUser().getFirstName() + " " + b.getUser().getLastName());
        } else {
            dto.setUsername(b.getFirstName() + " " + b.getLastName());
        }

        return dto;
    }

}
