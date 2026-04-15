package com.tracker.dao;

import com.tracker.model.Assignment;
import com.tracker.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AssignmentDAO {

    public boolean addAssignment(Assignment a) {
        String sql = "INSERT INTO assignments (title, description, subject, deadline, created_by) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, a.getTitle());
            ps.setString(2, a.getDescription());
            ps.setString(3, a.getSubject());
            ps.setTimestamp(4, Timestamp.valueOf(a.getDeadline()));
            ps.setInt(5, a.getCreatedBy());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[AssignmentDAO.addAssignment] " + e.getMessage());
            return false;
        }
    }

    public List<Assignment> getAllAssignments() {
        List<Assignment> list = new ArrayList<>();
        String sql = "SELECT a.*, u.name AS teacher_name FROM assignments a " +
                "JOIN users u ON a.created_by = u.user_id ORDER BY a.deadline";
        try (Statement st = DBConnection.getConnection().createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[AssignmentDAO.getAllAssignments] " + e.getMessage());
        }
        return list;
    }

    public List<Assignment> getAssignmentsByTeacher(int teacherId) {
        List<Assignment> list = new ArrayList<>();
        String sql = "SELECT a.*, u.name AS teacher_name FROM assignments a " +
                "JOIN users u ON a.created_by = u.user_id " +
                "WHERE a.created_by = ? ORDER BY a.deadline";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, teacherId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[AssignmentDAO.getByTeacher] " + e.getMessage());
        }
        return list;
    }

    public List<Assignment> getAssignmentsByStudent(int studentId) {
        List<Assignment> list = new ArrayList<>();
        String sql = "SELECT a.*, u.name AS teacher_name FROM assignments a " +
                "JOIN users u ON a.created_by = u.user_id " +
                "JOIN enrollments e ON a.assignment_id = e.assignment_id " +
                "WHERE e.student_id = ? ORDER BY a.deadline";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[AssignmentDAO.getByStudent] " + e.getMessage());
        }
        return list;
    }

    public Assignment getAssignmentById(int id) {
        String sql = "SELECT a.*, u.name AS teacher_name FROM assignments a " +
                "JOIN users u ON a.created_by = u.user_id " +
                "WHERE a.assignment_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("[AssignmentDAO.getById] " + e.getMessage());
        }
        return null;
    }

    public List<Assignment> getUpcomingDeadlines(int hours) {
        List<Assignment> list = new ArrayList<>();
        String sql = "SELECT DISTINCT a.*, u.name AS teacher_name FROM assignments a " +
                "JOIN users u ON a.created_by = u.user_id " +
                "JOIN enrollments e ON a.assignment_id = e.assignment_id " +
                "WHERE a.deadline BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL ? HOUR) " +
                "AND e.status = 'Pending'";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, hours);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[AssignmentDAO.getUpcomingDeadlines] " + e.getMessage());
        }
        return list;
    }

    public boolean updateAssignment(Assignment a) {
        String sql = "UPDATE assignments SET title=?, description=?, subject=?, deadline=? " +
                "WHERE assignment_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, a.getTitle());
            ps.setString(2, a.getDescription());
            ps.setString(3, a.getSubject());
            ps.setTimestamp(4, Timestamp.valueOf(a.getDeadline()));
            ps.setInt(5, a.getAssignmentId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[AssignmentDAO.updateAssignment] " + e.getMessage());
            return false;
        }
    }

    public boolean deleteAssignment(int id) {
        String sql = "DELETE FROM assignments WHERE assignment_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[AssignmentDAO.deleteAssignment] " + e.getMessage());
            return false;
        }
    }

    private Assignment mapRow(ResultSet rs) throws SQLException {
        Assignment a = new Assignment(
                rs.getInt("assignment_id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("subject"),
                rs.getTimestamp("deadline").toLocalDateTime(),
                rs.getInt("created_by")
        );
        a.setCreatedByName(rs.getString("teacher_name"));
        return a;
    }
}