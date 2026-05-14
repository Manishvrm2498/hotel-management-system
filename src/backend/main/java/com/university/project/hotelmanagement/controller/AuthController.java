package com.university.project.hotelmanagement.controller;

import com.university.project.hotelmanagement.dto.LoginRequest;
import com.university.project.hotelmanagement.dto.RegisterRequest;
import com.university.project.hotelmanagement.dto.ResetPasswordRequest;
import com.university.project.hotelmanagement.dto.SendOtpRequest;
import com.university.project.hotelmanagement.entity.UserEntity;
import com.university.project.hotelmanagement.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/auth")
public class AuthController {


    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody RegisterRequest request) {

        String token = authService.register(request);
        return ResponseEntity.ok(Map.of(
                "message", "OTP sent to your email for verification",
                "token", token
        ));
    }

    @PostMapping("/verify-signup")
    public ResponseEntity<?> verify(@RequestBody Map<String, String> request) {
        authService.confirmSignup(request.get("token"),request.get("otp"));
        return ResponseEntity.ok(Map.of("message","Registration successful! You can now login."));
    }

    @PostMapping("/register/resend")
    public ResponseEntity<?> resendOtp(@Valid @RequestBody SendOtpRequest email) {
        String token = authService.resendOtp(email,"REGISTRATION");
        if (token == null) {
            return ResponseEntity.ok(Map.of("message", "Account already verified"));
        }
        return ResponseEntity.ok(Map.of(
                "message", "OTP sent successfully",
                "token", token));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        String token = authService.login(loginRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login successful!");
        response.put("token", token);
        return ResponseEntity.ok(response);
    }


    @PutMapping("/update")
    public ResponseEntity<?> updateProfile(@RequestBody RegisterRequest request) {
        authService.update(request);
        return ResponseEntity.ok(Map.of("message", "Profile updated successfully!"));
    }


    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required");
        }
        String token = authService.forgotPassword(email);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "OTP sent successfully to your registered email.");
        response.put("token", token);

        return ResponseEntity.ok(response);
    }


    @PostMapping("/forgot-password/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        authService.passwordResetConfirm(request.get("token"),request.get("otp"));
        return ResponseEntity.ok(Map.of("message", "OTP verified successfully. You can now reset your password."));
    }


    @PostMapping("/forgot-password/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }

    @PostMapping("/forgot-password/resend")
    public ResponseEntity<String> resendForgotOtp(@RequestBody SendOtpRequest request) {
        String token = authService.resendOtp(request, "FORGOT_PASSWORD");
        return ResponseEntity.ok(token);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        authService.delete(id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }


}

