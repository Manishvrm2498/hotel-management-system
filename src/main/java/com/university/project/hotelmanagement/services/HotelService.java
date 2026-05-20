package com.university.project.hotelmanagement.services;

import com.university.project.hotelmanagement.dto.HotelRequestDTO;
import com.university.project.hotelmanagement.dto.HotelResponseDTO;
import com.university.project.hotelmanagement.dto.RoomRequest;
import com.university.project.hotelmanagement.dto.RoomResponseDTO;
import com.university.project.hotelmanagement.entity.Hotel;
import com.university.project.hotelmanagement.entity.Room;
import com.university.project.hotelmanagement.entity.UserEntity;
import com.university.project.hotelmanagement.exception.BadRequestException;
import com.university.project.hotelmanagement.exception.DuplicateResourceException;
import com.university.project.hotelmanagement.exception.InvalidCredentialsException;
import com.university.project.hotelmanagement.exception.ResourceNotFoundException;
import com.university.project.hotelmanagement.repository.BookingRepository;
import com.university.project.hotelmanagement.repository.HotelRepository;
import com.university.project.hotelmanagement.repository.RoomRepository;
import com.university.project.hotelmanagement.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HotelService {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public HotelService(HotelRepository hotelRepository, RoomRepository roomRepository, UserRepository userRepository, BookingRepository bookingRepository) {
        this.hotelRepository = hotelRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
    }


    public HotelResponseDTO saveHotel(HotelRequestDTO request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        UserEntity admin = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        boolean exists = hotelRepository.existsByNameAndAddress(
                request.getName().trim(),
                request.getAddress().trim()
        );
        if (exists) {
            throw new DuplicateResourceException("Hotel already exists");
        }
        Hotel hotel = new Hotel();
        hotel.setName(request.getName().trim());
        hotel.setState(request.getState().trim());
        hotel.setDistrict(request.getDistrict().trim());
        hotel.setAddress(request.getAddress().trim());
        hotel.setContactNumber(request.getContactNumber());
        hotel.setRating(request.getRating());
        hotel.setDescription(request.getDescription());
        hotel.setAdmin(admin);

        return mapToHotelDTO(hotelRepository.save(hotel));
    }


    public List<HotelResponseDTO> checkAvailability(String district, LocalDate checkIn, LocalDate checkOut) {

        if (checkIn.isBefore(LocalDate.now())) {
            throw new InvalidCredentialsException("Check-in date cannot be in the past");
        }
        if (!checkIn.isBefore(checkOut)) {
            throw new InvalidCredentialsException("Check-in date must be before Check-out date");
        }

        List<Hotel> hotels = hotelRepository.findByDistrict(district);
        if (hotels.isEmpty()) {
            throw new ResourceNotFoundException("No hotels found in district: " + district);
        }

        List<HotelResponseDTO> availableHotels = hotels.stream()
                .filter(hotel -> roomRepository.findByHotelId(hotel.getId()).stream()
                        .anyMatch(room -> room.isAvailable()
                                && room.getTotalRooms() > 0
                                && bookingRepository.countActiveOverlappingBookings(
                                room.getId(), checkIn, checkOut) < room.getTotalRooms()))
                .map(this::mapToHotelDTO)
                .collect(Collectors.toList());

        if (availableHotels.isEmpty()) {
            throw new ResourceNotFoundException("No availability for the selected dates");
        }

        return availableHotels;
    }


    public List<HotelResponseDTO> searchByLocation(String state, String district,String name) {
        return hotelRepository.findByStateDistrictAndName(state, district,name).stream()
                .map(this::mapToHotelDTO).collect(Collectors.toList());
    }

    public List<HotelResponseDTO> searchByName(String name) {
        return hotelRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::mapToHotelDTO)
                .collect(Collectors.toList());
    }

    public List<HotelResponseDTO> searchHotelByID(Long id) {
        return hotelRepository.findById(id)
                .stream()
                .map(this::mapToHotelDTO)
                .collect(Collectors.toList());

    }


    public List<HotelResponseDTO> searchHotels(String state, String district, Double rating) {

        List<Hotel> hotels = hotelRepository.findAll();

        return hotels.stream()

                // STATE FILTER
                .filter(hotel -> state == null ||
                        hotel.getState().toLowerCase().contains(state.toLowerCase()))

                // DISTRICT FILTER
                .filter(hotel -> district == null ||
                        hotel.getDistrict().toLowerCase().contains(district.toLowerCase()))

                // RATING FILTER
                .filter(hotel -> rating == null ||
                        hotel.getRating() >= rating)

                .map(hotel -> HotelResponseDTO.builder()
                        .id(hotel.getId())
                        .name(hotel.getName())
                        .district(hotel.getDistrict())
                        .address(hotel.getAddress())
                        .state(hotel.getState())   // 👈 IMPORTANT ADD
                        .rating(hotel.getRating())
                        .imageUrl(getHotelImageUrl(hotel))
                        .build())

                .toList();
    }

    public List<RoomResponseDTO> getRoomsByHotel(Long hotelId) {
        return roomRepository.findByHotelId(hotelId)
                .stream()
                .map(room -> {
                    RoomResponseDTO dto = new RoomResponseDTO();
                    dto.setId(room.getId());
                    dto.setType(room.getType());
                    dto.setPrice(room.getPrice());
                    dto.setTotalRooms(room.getTotalRooms());
                    dto.setHotelName(room.getHotel().getName());
                    dto.setHotelId(room.getHotel().getId());
                    dto.setAvailable(room.isAvailable());
                    dto.setImageUrl(getRoomImageUrl(room));
                    return dto;
                }).collect(Collectors.toList());
    }

    private HotelResponseDTO mapToHotelDTO(Hotel hotel) {
        HotelResponseDTO dto = new HotelResponseDTO();
        dto.setId(hotel.getId());
        dto.setName(hotel.getName());
        dto.setState(hotel.getState());
        dto.setDistrict(hotel.getDistrict());
        dto.setAddress(hotel.getAddress());
        dto.setContactNumber(hotel.getContactNumber());
        dto.setRating(hotel.getRating());
        dto.setDescription(hotel.getDescription());
        dto.setImageUrl(getHotelImageUrl(hotel));
        return dto;
    }
    private RoomResponseDTO mapToRoomDTO(Room r) {
        RoomResponseDTO dto = new RoomResponseDTO();
        dto.setId(r.getId());
        dto.setType(r.getType());
        dto.setPrice(r.getPrice());
        dto.setTotalRooms(r.getTotalRooms());
        dto.setHotelName(r.getHotel().getName());
        dto.setHotelId(r.getHotel().getId());
        dto.setAvailable(r.isAvailable());
        dto.setImageUrl(getRoomImageUrl(r));
        return dto;
    }


    public HotelResponseDTO updateHotel(Long id, HotelRequestDTO request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        if (!hotel.getAdmin().getEmail().equals(email)) {
            throw new AccessDeniedException("You are not allowed to modify this hotel");
        }

        hotel.setName(request.getName().trim());
        hotel.setState(request.getState().trim());
        hotel.setDistrict(request.getDistrict().trim());
        hotel.setAddress(request.getAddress().trim());
        hotel.setContactNumber(request.getContactNumber());
        hotel.setRating(request.getRating());
        hotel.setDescription(request.getDescription());

        return mapToHotelDTO(hotelRepository.save(hotel));
    }

    public HotelResponseDTO uploadHotelPicture(Long id, MultipartFile file) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        if (!hotel.getAdmin().getEmail().equals(email)) {
            throw new AccessDeniedException("You are not allowed to modify this hotel");
        }

        validateImage(file);
        hotel.setPicture(file.getBytes());
        hotel.setPictureContentType(file.getContentType());
        return mapToHotelDTO(hotelRepository.save(hotel));
    }

    public Optional<Hotel> getHotelEntity(Long id) {
        return hotelRepository.findById(id);
    }

    public String getHotelImageUrl(Hotel hotel) {
        return hotel.getPicture() == null ? null : "/api/hotels/" + hotel.getId() + "/picture";
    }

    public String getRoomImageUrl(Room room) {
        return room.getPicture() == null ? null : "/api/rooms/" + room.getId() + "/picture";
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Image file is required");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.matches("image/(jpeg|pjpeg|jpg|png)")) {
            throw new BadRequestException("Only JPG, JPEG, and PNG files are allowed");
        }
    }



}
