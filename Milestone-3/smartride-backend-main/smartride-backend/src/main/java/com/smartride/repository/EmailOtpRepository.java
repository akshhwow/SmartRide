package com.smartride.repository;

import com.smartride.entity.EmailOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailOtpRepository extends JpaRepository<EmailOtp, Long> {

    /**
     * Find OTP by email, code, and not used
     */
    Optional<EmailOtp> findByEmailAndOtpCodeAndIsUsedFalse(String email, String otpCode);

    /**
     * Find all OTPs by email
     */
    List<EmailOtp> findByEmail(String email);

    /**
     * Delete all OTPs for a specific email
     */
    @Modifying
    @Query("DELETE FROM EmailOtp e WHERE e.email = :email")
    void deleteAllByEmail(@Param("email") String email);

    /**
     * Find the latest OTP for an email
     */
    Optional<EmailOtp> findTopByEmailOrderByCreatedAtDesc(String email);

    /**
     * Find all unused OTPs for an email
     */
    List<EmailOtp> findByEmailAndIsUsedFalse(String email);
}