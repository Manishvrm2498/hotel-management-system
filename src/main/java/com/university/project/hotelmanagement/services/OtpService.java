package com.university.project.hotelmanagement.services;

import com.university.project.hotelmanagement.entity.Otp;
import com.university.project.hotelmanagement.exception.OtpException;
import com.university.project.hotelmanagement.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OtpService {

    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final OtpRepository otpRepository;
    private final EmailService emailService;
    private final RateLimitService rateLimitService;

    @Transactional
    public String sendOtp(String email,String username,String type) {
        String normalizedPurpose = type.trim().toUpperCase();

        rateLimitService.checkLimit(email);

        String otp = generateOtp();
        String token = UUID.randomUUID().toString();

        otpRepository.deleteByEmailAndPurpose(email, normalizedPurpose);

        Otp entity = new Otp();
        entity.setEmail(email);
        entity.setOtp(otp);
        entity.setOtpToken(token);
        entity.setPurpose(normalizedPurpose);
        entity.setExpiryTime(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        entity.setAttempts(0);
        entity.setVerified(false);

        otpRepository.save(entity);
        if ("FORGOT_PASSWORD".equalsIgnoreCase(normalizedPurpose)) {
            emailService.sendForgotPasswordEmail(email, username, otp);
        } else {
            emailService.sendOtpEmail(email, username, otp);
        }
        return token;
    }




    public Otp verifyOtp(String token, String enteredOtp, String expectedPurpose) {
        Otp otp = otpRepository.findByOtpToken(token)
                .orElseThrow(() -> new OtpException("Invalid token"));

        String normalizedPurpose = expectedPurpose.trim().toUpperCase();
        if (!normalizedPurpose.equalsIgnoreCase(otp.getPurpose())) {
            throw new OtpException("Invalid OTP purpose");
        }

        if (otp.isVerified()) {
            throw new OtpException("OTP already used");
        }
        if (otp.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new OtpException("OTP expired");
        }
        if (otp.getAttempts() >= 3) {
            throw new OtpException("Too many attempts. Request a new OTP.");
        }
        if (!otp.getOtp().equals(enteredOtp)) {
            otp.setAttempts(otp.getAttempts() + 1);
            otpRepository.save(otp);
            throw new OtpException("Invalid OTP. Attempts: " + otp.getAttempts());
        }
        otp.setVerified(true);
        rateLimitService.reset(otp.getEmail());
        return otpRepository.save(otp);
    }

    private String generateOtp() {
        return String.valueOf(100000 + RANDOM.nextInt(900000));
    }
}
