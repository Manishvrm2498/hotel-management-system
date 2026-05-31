package com.university.project.hotelmanagement.repository;

import com.university.project.hotelmanagement.entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {
    Optional<Otp> findByOtpToken(String token);

    @Modifying
    @Query("DELETE FROM Otp o WHERE o.email = :email AND o.purpose = :purpose")
    void deleteByEmailAndPurpose(@Param("email") String email,
                                 @Param("purpose") String purpose);
    void deleteByEmail(String email);
}
