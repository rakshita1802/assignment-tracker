package com.tracker.dao;

import com.tracker.model.Role;
import com.tracker.model.User;
import com.tracker.util.DBConnection;
import com.tracker.util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public User login(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password");
                // Support both plain text (old) and hashed (new) passwords
                if (PasswordUtil.verify(password, storedHash) || password.equals(storedHash)) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO.login] " + e.getMessage());
        }
        return null;
    }

    public boolean addUser(User user) {
        String sql = "INSERT INTO users (name, email, password, role, phone_number) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, PasswordUtil.hash(user.getPassword())); // ← hash here
            ps.setString(4, user.getRole().name());
            ps.setString(5, user.getPhoneNumber());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO.addUser] " + e.getMessage());
            return false;
        }
    }

    public boolean updatePassword(String email, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE email = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, PasswordUtil.hash(newPassword));
            ps.setString(2, email);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO.updatePassword] " + e.getMessage());
            return false;
        }
    }

    public boolean updateTheme(int userId, String theme) {
        String sql = "UPDATE users SET theme = ? WHERE user_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, theme);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO.updateTheme] " + e.getMessage());
            return false;
        }
    }


    public User getUserById(int id) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("[UserDAO.getUserById] " + e.getMessage());
        }
        return null;
    }

    public List<User> getUsersByRole(Role role) {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = ? ORDER BY name";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, role.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[UserDAO.getUsersByRole] " + e.getMessage());
        }
        return list;
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY role, name";
        try (Statement st = DBConnection.getConnection().createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[UserDAO.getAllUsers] " + e.getMessage());
        }
        return list;
    }

    public boolean updateUser(User user) {
        String sql = "UPDATE users SET name=?, email=?, password=?, role=?, phone_number=? WHERE user_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getRole().name());
            ps.setString(5, user.getPhoneNumber());
            ps.setInt(6, user.getUserId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO.updateUser] " + e.getMessage());
            return false;
        }
    }

    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO.deleteUser] " + e.getMessage());
            return false;
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("user_id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("password"),
                Role.valueOf(rs.getString("role")),
                rs.getString("phone_number")
        );
    }
}