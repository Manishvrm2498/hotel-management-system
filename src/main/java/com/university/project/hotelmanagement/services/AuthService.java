package com.university.project.hotelmanagement.services;

import com.university.project.hotelmanagement.dto.*;
import com.university.project.hotelmanagement.entity.Otp;
import com.university.project.hotelmanagement.entity.UserEntity;
import com.university.project.hotelmanagement.exception.*;
import com.university.project.hotelmanagement.repository.BookingRepository;
import com.university.project.hotelmanagement.repository.HotelRepository;
import com.university.project.hotelmanagement.repository.OtpRepository;
import com.university.project.hotelmanagement.repository.PaymentRepository;
import com.university.project.hotelmanagement.repository.ReviewRepository;
import com.university.project.hotelmanagement.repository.UserRepository;
import com.university.project.hotelmanagement.util.JwtUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;


@Service
public class AuthService {

    private static final int UNVERIFIED_USER_TTL_MINUTES = 15;

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final OtpService otpService;
    private final OtpRepository otpRepository;
    private final EmailService emailService;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final PaymentRepository paymentRepository;
    private final HotelRepository hotelRepository;

    public AuthService(
            UserRepository userRepository,
            JwtUtil jwtUtil,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            OtpService otpService,
            OtpRepository otpRepository,
            EmailService emailService,
            BookingRepository bookingRepository,
            ReviewRepository reviewRepository,
            PaymentRepository paymentRepository,
            HotelRepository hotelRepository
    ) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.otpService = otpService;
        this.otpRepository = otpRepository;
        this.emailService = emailService;
        this.bookingRepository = bookingRepository;
        this.reviewRepository = reviewRepository;
        this.paymentRepository = paymentRepository;
        this.hotelRepository = hotelRepository;
    }

    private UserEntity getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }


    public String register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        UserEntity user = userRepository.findByEmail(email).orElseGet(UserEntity::new);

        if (user.isEnabled()) {
            throw new DuplicateResourceException("Email already exists");
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("ROLE_USER");
        user.setEnabled(false);
        userRepository.save(user);
        return otpService.sendOtp(email, user.getFirstName(), "REGISTRATION");
    }




    public void confirmSignup(String token, String enteredOtp) {

        Otp otp = otpService.verifyOtp(token, enteredOtp, "REGISTRATION");

        UserEntity user = userRepository.findByEmail(otp.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setEnabled(true);
        userRepository.save(user);
        emailService.sendWelcomeEmail(user.getEmail(),user.getFirstName());

    }


    public String resendOtp(SendOtpRequest request, String purpose) {
        String email = request.getEmail().trim().toLowerCase();

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if ("REGISTRATION".equalsIgnoreCase(purpose) && user.isEnabled()) {
            throw new OtpException("Account already verified");
        }
        if ("FORGOT_PASSWORD".equalsIgnoreCase(purpose) && !user.isEnabled()) {
            throw new AccountNotVerifiedException("Account is not verified. Please complete signup verification first.");
        }
        return otpService.sendOtp(email, user.getFirstName(), purpose);
    }


    @Scheduled(fixedRate = 60000)
    @Transactional
    public void deleteUnverifiedUsers() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(UNVERIFIED_USER_TTL_MINUTES);
        userRepository.deleteStaleUnverifiedUsers(cutoff);
    }



    public String login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail().trim().toLowerCase(), request.getPassword())
            );
            return jwtUtil.generateToken(authentication);

        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException("Invalid email or password");
        } catch (DisabledException e) {
            throw new AccountNotVerifiedException("Account is not verified. Please verify your OTP first.");
        } catch (LockedException e) {
            throw new RuntimeException("Your account is locked. Please contact support.");
        } catch (AuthenticationException e) {
            throw new RuntimeException("Authentication failed: " + e.getMessage());
        }
    }

    public void update(RegisterRequest request) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        UserEntity userEntity = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getEmail() != null && !request.getEmail().equals(userEntity.getEmail())) {
            String normalizedEmail = request.getEmail().trim().toLowerCase();
            if (userRepository.existsByEmail(normalizedEmail)) {
                throw new DuplicateResourceException("Email already in use");
            }
            userEntity.setEmail(normalizedEmail);
        }

        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            userEntity.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            userEntity.setLastName(request.getLastName());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            userEntity.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        userRepository.save(userEntity);
    }

    public UserProfileDTO getProfile() {
        UserEntity user = getCurrentUser();
        return mapToProfileDTO(user);
    }

    public UserProfileDTO uploadProfilePicture(MultipartFile file) throws IOException {
        UserEntity user = getCurrentUser();
        validateImage(file);
        user.setPicture(file.getBytes());
        user.setPictureContentType(file.getContentType());
        return mapToProfileDTO(userRepository.save(user));
    }

    public UserEntity getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public List<UserProfileDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(UserEntity::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::mapToProfileDTO)
                .toList();
    }

    public UserProfileDTO updateUserRole(Long userId, String role) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getEmail().equals(currentUserEmail)) {
            throw new BadRequestException("You cannot change your own role.");
        }

        String normalizedRole = role == null ? "" : role.replace("ROLE_", "").trim().toUpperCase();
        if (!List.of("USER", "ADMIN", "SUPERADMIN").contains(normalizedRole)) {
            throw new BadRequestException("Role must be USER, ADMIN, or SUPERADMIN.");
        }

        user.setRole("ROLE_" + normalizedRole);
        return mapToProfileDTO(userRepository.save(user));
    }

    @Transactional
    public void deleteUserBySuperAdmin(Long userId) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.getEmail().equals(currentUserEmail)) {
            throw new BadRequestException("You cannot delete your own account.");
        }

        deleteAccount(user);
    }

    @Transactional
    public void deleteCurrentAccount() {
        deleteAccount(getCurrentUser());
    }

    @Transactional
    public void delete(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        String loggedInUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!user.getEmail().equals(loggedInUserEmail)) {
            throw new UnauthorizedAccessException("You can only delete your own account!");
        }
        deleteAccount(user);
    }

    private void deleteAccount(UserEntity user) {
        Long userId = user.getId();
        String email = user.getEmail();

        reviewRepository.deleteByUserOrBookingUserId(userId);
        paymentRepository.deleteByBookingUserId(userId);
        bookingRepository.deleteByUserId(userId);
        hotelRepository.clearAdminByUserId(userId);
        otpRepository.deleteByEmail(email);
        userRepository.delete(user);
    }



    public String forgotPassword(String email) {
        String normalizedEmail = email.trim().toLowerCase();

        UserEntity user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.isEnabled()) {
            throw new AccountNotVerifiedException("Account is not verified. Please complete signup verification first.");
        }

       return otpService.sendOtp(normalizedEmail, user.getFirstName(), "FORGOT_PASSWORD");
    }


    public void passwordResetConfirm(String token, String enteredOtp) {
        otpService.verifyOtp(token, enteredOtp, "FORGOT_PASSWORD");
    }



    public void resetPassword(ResetPasswordRequest request) {

        String token = request.getToken();
        String newPassword = request.getNewPassword();
        String confirmPassword = request.getConfirmPassword();

        if (newPassword == null || !newPassword.equals(confirmPassword)) {
            throw new BadRequestException("Passwords do not match");
        }

        if (!newPassword.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$")) {
            throw new BadRequestException("Password must be at least 8 characters and include digit, upper, lower, and special char.");
        }

        Otp otpEntry = otpRepository.findByOtpToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid or expired session token"));

        if (!"FORGOT_PASSWORD".equalsIgnoreCase(otpEntry.getPurpose())) {
            throw new BadRequestException("Invalid password reset session");
        }
        if (!otpEntry.isVerified()) {
            throw new BadRequestException("OTP verification required before resetting password");
        }
        if (otpEntry.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Password reset session expired");
        }

        UserEntity user = userRepository.findByEmail(otpEntry.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        otpRepository.delete(otpEntry);
    }

    private UserProfileDTO mapToProfileDTO(UserEntity user) {
        return UserProfileDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .imageUrl(user.getPicture() == null ? null : "/api/users/" + user.getId() + "/picture")
                .imageVersion(user.getPicture() == null ? null : System.currentTimeMillis())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
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
