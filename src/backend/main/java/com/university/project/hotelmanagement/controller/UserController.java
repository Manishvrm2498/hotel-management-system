package com.university.project.hotelmanagement.controller;

import com.university.project.hotelmanagement.dto.UserProfileDTO;
import com.university.project.hotelmanagement.entity.UserEntity;
import com.university.project.hotelmanagement.services.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> profile() {
        return ResponseEntity.ok(authService.getProfile());
    }

    @PostMapping(value = "/profile/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserProfileDTO> uploadProfilePicture(@RequestPart("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(authService.uploadProfilePicture(file));
    }

    @DeleteMapping("/profile")
    public ResponseEntity<?> deleteProfile() {
        authService.deleteCurrentAccount();
        return ResponseEntity.ok().body(java.util.Map.of("message", "Account deleted successfully"));
    }

    @GetMapping("/{id}/picture")
    public ResponseEntity<byte[]> picture(@PathVariable Long id) {
        UserEntity user = authService.getUserById(id);
        if (user.getPicture() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate, max-age=0")
                .contentType(MediaType.parseMediaType(user.getPictureContentType()))
                .body(user.getPicture());
    }
}
