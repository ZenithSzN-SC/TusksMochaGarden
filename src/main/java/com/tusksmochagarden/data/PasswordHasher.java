package com.tusksmochagarden.data;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Bcrypt-backed password hashing. verify() falls back to a plaintext comparison for
 * rows stored before this class existed, so old accounts still log in — callers should
 * rehash via hash() on that fallback success to migrate the row off plaintext.
 */
public final class PasswordHasher {

    private PasswordHasher() {
    }

    public static String hash(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    public static boolean verify(String rawPassword, String stored) {
        if (isHashed(stored)) {
            return BCrypt.checkpw(rawPassword, stored);
        }
        return rawPassword.equals(stored);
    }

    public static boolean isHashed(String stored) {
        return stored != null
                && (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$"));
    }
}
