package com.tracker.dao;

import com.tracker.model.Notification;
import com.tracker.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    public boolean addNotification(Notification n) {
        String sql = "INSERT INTO notifications (student_id, message, sent_time, status) VALUES (?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, n.getStudentId());
            ps.setString(2, n.getMessage());
            ps.setTimestamp(3, Timestamp.valueOf(n.getSentTime()));
            ps.setString(4, n.getStatus());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[NotificationDAO.add] " + e.getMessage());
            return false;
        }
    }

    public List<Notification> getAllNotifications() {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT n.*, u.name AS student_name FROM notifications n " +
                "JOIN users u ON n.student_id = u.user_id " +
                "ORDER BY n.sent_time DESC";
        try (Statement st = DBConnection.getConnection().createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[NotificationDAO.getAll] " + e.getMessage());
        }
        return list;
    }

    public List<Notification> getNotificationsForStudent(int studentId) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT n.*, u.name AS student_name FROM notifications n " +
                "JOIN users u ON n.student_id = u.user_id " +
                "WHERE n.student_id = ? ORDER BY n.sent_time DESC";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[NotificationDAO.getForStudent] " + e.getMessage());
        }
        return list;
    }

    public boolean markRead(int notificationId) {
        String sql = "UPDATE notifications SET status='Read' WHERE notification_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, notificationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[NotificationDAO.markRead] " + e.getMessage());
            return false;
        }
    }

    private Notification mapRow(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setNotificationId(rs.getInt("notification_id"));
        n.setStudentId(rs.getInt("student_id"));
        n.setMessage(rs.getString("message"));
        n.setSentTime(rs.getTimestamp("sent_time").toLocalDateTime());
        n.setStatus(rs.getString("status"));
        n.setStudentName(rs.getString("student_name"));
        return n;
    }
}