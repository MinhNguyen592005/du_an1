package com.vu.springapi.repository;

import com.vu.springapi.model.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {
    Optional<OtpToken> findByEmailAndOtpAndUsedFalseAndExpiryTimeAfter(
            String email,
            String otp,
            LocalDateTime currentTime
    );

    void deleteByExpiryTimeBefore(LocalDateTime currentTime);
}
