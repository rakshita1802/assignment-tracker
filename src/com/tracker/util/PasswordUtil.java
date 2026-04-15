package com.tracker.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility for hashing and verifying passwords using BCrypt.
 */
public class PasswordUtil {

    private static final int WORK_FACTOR = 12;

    /** Hashes a plain-text password. Store the returned string in DB. */
    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(WORK_FACTOR));
    }

    /** Returns true if plainPassword matches the stored hash. */
    public static boolean verify(String plainPassword, String storedHash) {
        try {
            return BCrypt.checkpw(plainPassword, storedHash);
        } catch (Exception e) {
            return false;
        }
    }

    private PasswordUtil() {}
}