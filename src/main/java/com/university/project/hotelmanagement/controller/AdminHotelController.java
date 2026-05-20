package com.university.project.hotelmanagement.controller;

import com.university.project.hotelmanagement.dto.*;
import com.university.project.hotelmanagement.services.AuthService;
import com.university.project.hotelmanagement.services.BookingService;
import com.university.project.hotelmanagement.services.HotelService;
import com.university.project.hotelmanagement.services.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminHotelController {


    private final HotelService hotelService;
    private final BookingService bookingService;
    private final RoomService roomService;
    private final AuthService authService;

    public AdminHotelController(HotelService hotelService, BookingService bookingService, RoomService roomService, AuthService authService) {
        this.hotelService = hotelService;
        this.bookingService = bookingService;
        this.roomService = roomService;
        this.authService = authService;
    }

    @PostMapping("/add")
    public ResponseEntity<HotelResponseDTO> addHotel(@RequestBody HotelRequestDTO hotelDto) {
        return ResponseEntity.ok(hotelService.saveHotel(hotelDto));
    }

    @PutMapping("/update/{hotelId}")
    public ResponseEntity<HotelResponseDTO> updateHotel(@Valid @RequestBody HotelRequestDTO hotelDto, @PathVariable Long hotelId) {
        return ResponseEntity.ok(hotelService.updateHotel(hotelId, hotelDto));
    }

    @PostMapping(value = "/hotels/{hotelId}/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<HotelResponseDTO> uploadHotelPicture(@PathVariable Long hotelId, @RequestPart("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(hotelService.uploadHotelPicture(hotelId, file));
    }

    @PostMapping("/add-room")
    public ResponseEntity<RoomResponseDTO> addRoom(@Valid @RequestBody RoomRequest roomDto) {
        return ResponseEntity.ok(roomService.saveRoom(roomDto));
    }

    @PatchMapping("/update/{roomId}")
    public ResponseEntity<RoomResponseDTO> createRoom(@PathVariable Long roomId,@Valid @RequestBody RoomRequest roomRequest) {
        return ResponseEntity.ok(roomService.updateRoom(roomId,roomRequest));
    }

    @PostMapping(value = "/rooms/{roomId}/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RoomResponseDTO> uploadRoomPicture(@PathVariable Long roomId, @RequestPart("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(roomService.uploadRoomPicture(roomId, file));
    }

    @PreAuthorize("hasRole('SUPERADMIN')")
    @GetMapping("/user-list")
    public ResponseEntity<List<UserProfileDTO>> getUsers() {
        return ResponseEntity.ok(authService.getAllUsers());
    }

    @PreAuthorize("hasRole('SUPERADMIN')")
    @PatchMapping("/users/{userId}/role")
    public ResponseEntity<UserProfileDTO> updateUserRole(@PathVariable Long userId, @RequestBody Map<String, String> request) {
        return ResponseEntity.ok(authService.updateUserRole(userId, request.get("role")));
    }

    @PreAuthorize("hasRole('SUPERADMIN')")
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long userId) {
        authService.deleteUserBySuperAdmin(userId);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDTO> getBookingDetails(@PathVariable Long bookingId) {
        BookingResponseDTO response = bookingService.getBookingByIdForAdmin(bookingId);
        return ResponseEntity.ok(response);
    }

}
