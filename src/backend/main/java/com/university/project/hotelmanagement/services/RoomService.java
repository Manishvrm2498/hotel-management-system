package com.university.project.hotelmanagement.services;

import com.university.project.hotelmanagement.dto.RoomRequest;
import com.university.project.hotelmanagement.dto.RoomResponseDTO;
import com.university.project.hotelmanagement.entity.Hotel;
import com.university.project.hotelmanagement.entity.Room;
import com.university.project.hotelmanagement.enums.RoomType;
import com.university.project.hotelmanagement.exception.DuplicateResourceException;
import com.university.project.hotelmanagement.exception.ResourceNotFoundException;
import com.university.project.hotelmanagement.exception.UnauthorizedAccessException;
import com.university.project.hotelmanagement.repository.HotelRepository;
import com.university.project.hotelmanagement.repository.RoomRepository;
import com.university.project.hotelmanagement.exception.BadRequestException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
public class RoomService {


    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;

    public RoomService(RoomRepository roomRepository, HotelRepository hotelRepository) {
        this.roomRepository = roomRepository;
        this.hotelRepository = hotelRepository;
    }

    public RoomResponseDTO getRoomById(Long roomId) {
        SecurityContextHolder.getContext().getAuthentication();
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + roomId));

        return mapToResponseDTO(room);
    }

    @Transactional
    public RoomResponseDTO saveRoom(RoomRequest roomDto) {
        Hotel hotel = hotelRepository.findById(roomDto.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: " + roomDto.getHotelId()));

        boolean exists = roomRepository.existsByHotelIdAndType(roomDto.getHotelId(), roomDto.getType());

        if (exists) {
            throw new DuplicateResourceException("Room type '" + roomDto.getType() + "' already exists for this hotel");
        }

        Room room = new Room();
        room.setHotel(hotel);
        room.setType(roomDto.getType());
        room.setPrice(roomDto.getPrice());
        room.setTotalRooms(roomDto.getTotalRooms());
        room.setAvailable(true);

        Room savedRoom = roomRepository.save(room);
        return mapToResponseDTO(savedRoom);
    }

    @Transactional
    public RoomResponseDTO updateRoom(Long roomId, RoomRequest roomDto) {

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + roomId));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        String adminEmail = room.getHotel().getAdmin().getEmail();

        if (adminEmail == null || !adminEmail.equals(currentUsername)) {
            throw new UnauthorizedAccessException("You are not allowed to update this room.");
        }
        if (roomDto.getType() != null) {
            room.setType(roomDto.getType());
        }
        if (roomDto.getPrice() != null) {
            room.setPrice(roomDto.getPrice());
        }
        if (roomDto.getTotalRooms() != null) {
            room.setTotalRooms(roomDto.getTotalRooms());
        }
        room.setAvailable(room.getTotalRooms() > 0);
        Room updatedRoom = roomRepository.save(room);

        return mapToResponseDTO(updatedRoom);
    }

    @Transactional
    public RoomResponseDTO uploadRoomPicture(Long roomId, MultipartFile file) throws IOException {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + roomId));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        String adminEmail = room.getHotel().getAdmin().getEmail();
        if (adminEmail == null || !adminEmail.equals(currentUsername)) {
            throw new UnauthorizedAccessException("You are not allowed to update this room.");
        }

        validateImage(file);
        room.setPicture(file.getBytes());
        room.setPictureContentType(file.getContentType());
        return mapToResponseDTO(roomRepository.save(room));
    }

    public Optional<Room> getRoomEntity(Long roomId) {
        return roomRepository.findById(roomId);
    }


//    public void deleteRoom(Long roomId, UserDetails userDetails) {
//        Room room = roomRepository.findById(roomId)
//                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
//
//        // SECURITY: Ownership check
//        if (!room.getHotel().getAdmin().getEmail().equals(userDetails.getUsername())) {
//            throw new UnauthorizedAccessException("Unauthorized to delete this room.");
//        }
//
//        roomRepository.delete(room);
//    }

    public RoomResponseDTO mapToResponseDTO(Room room) {

        return RoomResponseDTO.builder()
                .id(room.getId())
                .type(room.getType())
                .price(room.getPrice())
                .totalRooms(room.getTotalRooms())
                .hotelId(room.getHotel() != null ? room.getHotel().getId() : null)
                .hotelName(room.getHotel() != null ? room.getHotel().getName() : null)
                .isAvailable(room.isAvailable())
                .imageUrl(room.getPicture() == null ? null : "/api/rooms/" + room.getId() + "/picture")
                .build();
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
