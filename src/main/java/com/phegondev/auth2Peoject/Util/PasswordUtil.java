package com.phegondev.auth2Peoject.Util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


public class PasswordUtil {

    // Static encoder instance (thread-safe)
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * Hashes a raw password using BCrypt.
     *
     * @param rawPassword the plain text password
     * @return the hashed password
     */
    public static String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    /**
     * Checks whether a raw password matches a previously encoded password.
     *
     * @param rawPassword the plain password to check
     * @param encodedPassword the hashed password stored in DB
     * @return true if passwords match, false otherwise
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }



}
