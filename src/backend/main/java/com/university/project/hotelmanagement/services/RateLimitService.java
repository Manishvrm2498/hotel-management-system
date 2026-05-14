package com.university.project.hotelmanagement.services;
import com.university.project.hotelmanagement.exception.BadRequestException;
import com.university.project.hotelmanagement.exception.OtpException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private final Map<String, Integer> attempts = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> blockedUntil = new ConcurrentHashMap<>();

    private static final int MAX_ATTEMPTS = 3;
    private static final int BLOCK_MINUTES = 1;

    public void checkLimit(String email) {

        LocalDateTime now = LocalDateTime.now();

        if (blockedUntil.containsKey(email)) {
            LocalDateTime unblockTime = blockedUntil.get(email);

            if (unblockTime.isAfter(now)) {
                throw new OtpException("Too many attempts. Try again after " +
                        java.time.Duration.between(now, unblockTime).toMinutes() + " minutes.");
            } else {
                blockedUntil.remove(email);
                attempts.remove(email);
            }
        }

        int count = attempts.getOrDefault(email, 0);

        if (count >= MAX_ATTEMPTS) {
            blockedUntil.put(email, now.plusMinutes(BLOCK_MINUTES));
            attempts.remove(email);

            throw new OtpException("Too many OTP requests. You are blocked for " + BLOCK_MINUTES + " minutes.");
        }

        attempts.put(email, count + 1);
    }

    public void reset(String email) {
        attempts.remove(email);
        blockedUntil.remove(email);
    }
}