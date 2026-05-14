package com.university.project.hotelmanagement.repository;

import com.university.project.hotelmanagement.entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {
    Optional<Otp> findByOtpToken(String token);

    void deleteByEmailAndPurpose(String email, String purpose);

    void deleteByEmail(String email);
}
