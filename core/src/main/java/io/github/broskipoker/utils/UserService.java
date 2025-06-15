package io.github.broskipoker.utils;

import io.github.broskipoker.game.User;
import java.sql.*;
import java.util.Optional;

public class UserService {
    private static UserService instance;
    private final DatabaseConnection dbConnection;
    private User currentUser;

    private UserService() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    public static UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    /**
     * Register a new user
     * @param username The username
     * @param email The email address
     * @param password The plaintext password (will be hashed)
     * @return true if registration successful, false otherwise
     */
    public boolean registerUser(String username, String email, String password) {
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            return false;
        }

        String hashedPassword = PasswordUtils.hashPassword(password);
        String sql = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, username.trim());
            stmt.setString(2, email != null ? email.trim() : null);
            stmt.setString(3, hashedPassword);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Login a user
     * @param username The username
     * @param password The plaintext password
     * @return true if login successful, false otherwise
     */
    public boolean loginUser(String username, String password) {
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            return false;
        }

        String sql = "SELECT id, username, email, password, chips, games_played, wins, losses FROM users WHERE username = ?";

        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, username.trim());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String hashedPassword = rs.getString("password");

                if (PasswordUtils.checkPassword(password, hashedPassword)) {
                    // Create user object and set as current user
                    currentUser = new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getLong("chips"),
                        rs.getInt("games_played"),
                        rs.getInt("wins"),
                        rs.getInt("losses")
                    );
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error logging in user: " + e.getMessage());
        }

        return false;
    }

    /**
     * Logout the current user
     */
    public void logoutUser() {
        currentUser = null;
    }

    /**
     * Check if a user is currently logged in
     * @return true if logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public User getCurrentUserOrThrow() {
        if (currentUser == null) {
            throw new IllegalStateException("No user is currently logged in");
        }
        return currentUser;
    }

    /**
     * Get the current logged-in user
     * @return Optional containing the current user, or empty if not logged in
     */
    public Optional<User> getCurrentUser() {
        return Optional.ofNullable(currentUser);
    }

    /**
     * Update user's chip count
     * @param newChipCount The new chip count
     * @return true if update successful, false otherwise
     */
    public boolean updateUserChips(long newChipCount) {
        if (currentUser == null) {
            return false;
        }

        String sql = "UPDATE users SET chips = ? WHERE id = ?";

        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, newChipCount);
            stmt.setInt(2, currentUser.getId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                currentUser.setChips(newChipCount);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error updating user chips: " + e.getMessage());
        }

        return false;
    }

    /**
     * Update user's game statistics
     * @param won Whether the game was won
     * @return true if update successful, false otherwise
     */
    public boolean updateGameStats(boolean won) {
        if (currentUser == null) {
            return false;
        }

        String sql = "UPDATE users SET games_played = games_played + 1, " +
            (won ? "wins = wins + 1" : "losses = losses + 1") +
            " WHERE id = ?";

        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, currentUser.getId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                currentUser.setGamesPlayed(currentUser.getGamesPlayed() + 1);
                if (won) {
                    currentUser.setWins(currentUser.getWins() + 1);
                } else {
                    currentUser.setLosses(currentUser.getLosses() + 1);
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error updating game stats: " + e.getMessage());
        }

        return false;
    }

    /**
     * Check if username already exists
     * @param username The username to check
     * @return true if username exists, false otherwise
     */
    public boolean usernameExists(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        String sql = "SELECT 1 FROM users WHERE username = ?";

        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, username.trim());
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Error checking username: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if email already exists
     * @param email The email to check
     * @return true if email exists, false otherwise
     */
    public boolean emailExists(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        String sql = "SELECT 1 FROM users WHERE email = ?";

        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, email.trim());
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Error checking email: " + e.getMessage());
            return false;
        }
    }
}
