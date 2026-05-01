package org.BOGO.repository;

import org.BOGO.config.DatabaseConfig;
import org.BOGO.domain.common.PersonalDetails;

import java.sql.*;

/**
 * JDBC repository for User-related DB operations.
 * Handles read/write for the Users table and session tokens.
 */
public class UserRepository {

    /**
     * Finds a user's row by email address.
     * Returns a PersonalDetails object populated from the DB, or null if not found.
     */
    public PersonalDetails findByEmail(String email) {
        String sql = "SELECT userID, name, email, phoneNumber, password FROM Users WHERE email = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PersonalDetails pd = new PersonalDetails(
                        rs.getInt("userID"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phoneNumber"),
                        rs.getString("password")
                    );
                    return pd;
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserRepository] findByEmail failed: " + e.getMessage());
        }
        return null;
    }

    /**
     * Finds a user's row by userID.
     * Returns a PersonalDetails object populated from the DB, or null if not found.
     */
    public PersonalDetails findById(int userID) {
        String sql = "SELECT userID, name, email, phoneNumber, password FROM Users WHERE userID = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new PersonalDetails(
                        rs.getInt("userID"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phoneNumber"),
                        rs.getString("password")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserRepository] findById failed: " + e.getMessage());
        }
        return null;
    }

    /**
     * Returns the userType ('Admin', 'Driver', 'Passenger') for the given userID.
     */
    public String getUserType(int userID) {
        String sql = "SELECT userType FROM Users WHERE userID = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("userType");
            }
        } catch (SQLException e) {
            System.err.println("[UserRepository] getUserType failed: " + e.getMessage());
        }
        return null;
    }

    /**
     * Inserts a new user row and returns the generated userID, or -1 on failure.
     */
    public int save(PersonalDetails details, String role) {
        String sql = "INSERT INTO Users (name, email, phoneNumber, password, userType) " +
                     "VALUES (?, ?, ?, ?, ?); SELECT SCOPE_IDENTITY();";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, details.getName());
            ps.setString(2, details.getEmail());
            ps.setString(3, details.getPhoneNumber());
            ps.setString(4, details.getPassword());
            ps.setString(5, role);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[UserRepository] save failed: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Updates the password for the given email address.
     */
    public boolean updatePassword(String email, String newPassword) {
        String sql = "UPDATE Users SET password = ? WHERE email = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setString(2, email);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserRepository] updatePassword failed: " + e.getMessage());
        }
        return false;
    }

    /**
     * Persists a session token for the given userID into a SessionTokens table.
     * The Sessions table is created lazily if it does not yet exist.
     */
    public void saveSessionToken(int userID, String token) {
        ensureSessionTableExists();
        String sql = "INSERT INTO SessionTokens (userID, token, createdAt) VALUES (?, ?, GETDATE())";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userID);
            ps.setString(2, token);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[UserRepository] saveSessionToken failed: " + e.getMessage());
        }
    }

    /**
     * Deletes all session tokens for the given userID (logout).
     */
    public void deleteSessionToken(int userID) {
        ensureSessionTableExists();
        String sql = "DELETE FROM SessionTokens WHERE userID = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userID);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[UserRepository] deleteSessionToken failed: " + e.getMessage());
        }
    }

    /**
     * Returns true if the token exists and belongs to a known user (is valid/active).
     */
    public boolean sessionTokenExists(String token) {
        ensureSessionTableExists();
        String sql = "SELECT COUNT(*) FROM SessionTokens WHERE token = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("[UserRepository] sessionTokenExists failed: " + e.getMessage());
        }
        return false;
    }

    /** Creates the SessionTokens table on first use if it doesn't exist. */
    private void ensureSessionTableExists() {
        String ddl = "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='SessionTokens' AND xtype='U') " +
                     "CREATE TABLE SessionTokens (" +
                     "  tokenID  INT IDENTITY(1,1) PRIMARY KEY," +
                     "  userID   INT NOT NULL FOREIGN KEY REFERENCES Users(userID)," +
                     "  token    VARCHAR(512) NOT NULL UNIQUE," +
                     "  createdAt DATETIME DEFAULT GETDATE()" +
                     ")";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement()) {
            st.execute(ddl);
        } catch (SQLException e) {
            System.err.println("[UserRepository] ensureSessionTableExists failed: " + e.getMessage());
        }
    }
}
