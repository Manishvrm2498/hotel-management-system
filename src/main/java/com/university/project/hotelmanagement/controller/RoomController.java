package com.university.project.hotelmanagement.controller;

import com.university.project.hotelmanagement.dto.RoomRequest;
import com.university.project.hotelmanagement.dto.RoomResponseDTO;
import com.university.project.hotelmanagement.entity.Room;
import com.university.project.hotelmanagement.exception.ResourceNotFoundException;
import com.university.project.hotelmanagement.services.RoomService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {


    private  final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomResponseDTO> getRoom(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }

    @GetMapping("/{id}/picture")
    public ResponseEntity<byte[]> getRoomPicture(@PathVariable Long id) {
        Room room = roomService.getRoomEntity(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + id));
        if (room.getPicture() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                .contentType(MediaType.parseMediaType(room.getPictureContentType()))
                .body(room.getPicture());
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateRoom(@PathVariable Long id,
                                             @RequestBody RoomRequest roomDto) {
        roomService.updateRoom(id, roomDto);
        return ResponseEntity.ok("Room updated successfully!");
    }

    // Delete
//    @DeleteMapping("/{id}")
//    public ResponseEntity<String> deleteRoom(@PathVariable Long id,
//                                             @AuthenticationPrincipal UserDetails userDetails) {
//        roomService.deleteRoom(id, userDetails);
//        return ResponseEntity.ok("Room deleted successfully!");
//    }
}
