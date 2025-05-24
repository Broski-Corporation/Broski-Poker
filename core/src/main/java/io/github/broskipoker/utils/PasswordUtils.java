package io.github.broskipoker.utils;
import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {

    // Hash a password using BCrypt
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    // Check if a plaintext password matches a hashed password
    public static boolean checkPassword(String plaintextPassword, String hashedPassword) {
        return BCrypt.checkpw(plaintextPassword, hashedPassword);
    }
}
