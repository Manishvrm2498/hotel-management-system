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
import com.university.project.hotelmanagement.repository.BookingRepository.AdminBookingRow;
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
import java.util.Locale;
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

        List<Booking> completedBookings =
                bookingRepository.findByStatusAndCheckOutDateBefore(
                        BookingStatus.CONFIRMED,
                        LocalDate.now()
                );

        for (Booking booking : completedBookings) {
            booking.setStatus(BookingStatus.COMPLETED);
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
        AdminBookingRow row = bookingRepository.findAdminBookingRowById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        UserEntity currentUser = getCurrentUser();
        String adminEmail = row.getAdminEmail();
        boolean isSuperAdmin = "ROLE_SUPERADMIN".equalsIgnoreCase(currentUser.getRole());
        if (!isSuperAdmin && (adminEmail == null || !adminEmail.equals(currentUser.getEmail()))) {
            throw new ResourceNotFoundException("Booking not found with ID: " + bookingId);
        }

        return mapAdminBookingRow(row);
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
        dto.setFirstName(b.getFirstName());
        dto.setLastName(b.getLastName());
        dto.setEmail(b.getEmail());
        dto.setUserId(b.getUser() == null ? null : b.getUser().getId());
        dto.setUserEmail(b.getUser() == null ? null : b.getUser().getEmail());
        dto.setPhoneNumber(b.getPhoneNumber());
        dto.setTotalGuests(b.getTotalGuests());

        enrichCustomerDetails(b, dto);

        return dto;
    }

    private void enrichCustomerDetails(Booking booking, BookingResponseDTO dto) {
        Optional<UserEntity> matchedUser = Optional.ofNullable(booking.getUser());

        if (matchedUser.isEmpty() && hasText(dto.getFirstName()) && hasText(dto.getLastName())) {
            matchedUser = userRepository.findFirstByFirstNameIgnoreCaseAndLastNameIgnoreCase(
                    dto.getFirstName().trim(),
                    dto.getLastName().trim()
            );
        }

        if (matchedUser.isEmpty() && hasText(dto.getFirstName() + dto.getLastName())) {
            String bookingName = normalizeName(dto.getFirstName(), dto.getLastName());
            matchedUser = userRepository.findAll()
                    .stream()
                    .filter(user -> normalizeName(user.getFirstName(), user.getLastName()).equals(bookingName))
                    .findFirst();
        }

        matchedUser.ifPresent(user -> {
            dto.setUserId(user.getId());
            dto.setUserEmail(user.getEmail());
            if (!hasText(dto.getFirstName())) {
                dto.setFirstName(user.getFirstName());
            }
            if (!hasText(dto.getLastName())) {
                dto.setLastName(user.getLastName());
            }
            if (!hasText(dto.getEmail())) {
                dto.setEmail(user.getEmail());
            }
        });

        String name = (safe(dto.getFirstName()) + " " + safe(dto.getLastName())).trim();
        dto.setUsername(hasText(name) ? name : "Guest");
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String normalizeName(String firstName, String lastName) {
        return (safe(firstName) + " " + safe(lastName))
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "");
    }

    private BookingResponseDTO mapAdminBookingRow(AdminBookingRow row) {
        BookingResponseDTO dto = new BookingResponseDTO();
        dto.setId(row.getId());
        dto.setFirstName(row.getFirstName());
        dto.setLastName(row.getLastName());
        dto.setEmail(row.getEmail());
        dto.setPhoneNumber(row.getPhoneNumber());
        dto.setTotalGuests(row.getTotalGuests() == null ? 0 : row.getTotalGuests());
        dto.setCheckInDate(row.getCheckInDate());
        dto.setCheckOutDate(row.getCheckOutDate());
        dto.setTotalPrice(row.getTotalPrice() == null ? 0 : row.getTotalPrice());
        dto.setStatus(row.getStatus());
        dto.setBookedAt(row.getBookedAt());
        dto.setUserId(row.getUserId());
        dto.setUserEmail(row.getUserEmail());
        dto.setHotelName(row.getHotelName());
        dto.setRoomType(row.getRoomType());
        if (!hasText(dto.getEmail())) {
            dto.setEmail(dto.getUserEmail());
        }
        String name = (safe(dto.getFirstName()) + " " + safe(dto.getLastName())).trim();
        dto.setUsername(hasText(name) ? name : "Guest");
        return dto;
    }

}
