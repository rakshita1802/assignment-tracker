package com.tracker.dao;

import com.tracker.model.Enrollment;
import com.tracker.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentDAO {

    public boolean enrollStudent(int studentId, int assignmentId) {
        String sql = "INSERT INTO enrollments (student_id, assignment_id, status) VALUES (?,?,'Pending')";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, assignmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[EnrollmentDAO.enrollStudent] " + e.getMessage());
            return false;
        }
    }

    public List<Enrollment> getAllEnrollments() {
        List<Enrollment> list = new ArrayList<>();
        String sql = "SELECT e.*, u.name AS student_name, a.title AS assignment_title " +
                "FROM enrollments e " +
                "JOIN users u ON e.student_id = u.user_id " +
                "JOIN assignments a ON e.assignment_id = a.assignment_id " +
                "ORDER BY u.name, a.title";
        try (Statement st = DBConnection.getConnection().createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[EnrollmentDAO.getAllEnrollments] " + e.getMessage());
        }
        return list;
    }

    public List<Enrollment> getEnrollmentsByStudent(int studentId) {
        List<Enrollment> list = new ArrayList<>();
        String sql = "SELECT e.*, u.name AS student_name, a.title AS assignment_title " +
                "FROM enrollments e " +
                "JOIN users u ON e.student_id = u.user_id " +
                "JOIN assignments a ON e.assignment_id = a.assignment_id " +
                "WHERE e.student_id = ? ORDER BY a.title";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[EnrollmentDAO.getByStudent] " + e.getMessage());
        }
        return list;
    }

    public List<Enrollment> getEnrollmentsByAssignment(int assignmentId) {
        List<Enrollment> list = new ArrayList<>();
        String sql = "SELECT e.*, u.name AS student_name, a.title AS assignment_title " +
                "FROM enrollments e " +
                "JOIN users u ON e.student_id = u.user_id " +
                "JOIN assignments a ON e.assignment_id = a.assignment_id " +
                "WHERE e.assignment_id = ? ORDER BY u.name";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, assignmentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[EnrollmentDAO.getByAssignment] " + e.getMessage());
        }
        return list;
    }

    public boolean markCompleted(int studentId, int assignmentId) {
        String sql = "UPDATE enrollments SET status='Completed' " +
                "WHERE student_id=? AND assignment_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, assignmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[EnrollmentDAO.markCompleted] " + e.getMessage());
            return false;
        }
    }

    public boolean removeEnrollment(int studentId, int assignmentId) {
        String sql = "DELETE FROM enrollments WHERE student_id=? AND assignment_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, assignmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[EnrollmentDAO.removeEnrollment] " + e.getMessage());
            return false;
        }
    }

    private Enrollment mapRow(ResultSet rs) throws SQLException {
        Enrollment e = new Enrollment(
                rs.getInt("enrollment_id"),
                rs.getInt("student_id"),
                rs.getInt("assignment_id"),
                rs.getString("status")
        );
        e.setStudentName(rs.getString("student_name"));
        e.setAssignmentTitle(rs.getString("assignment_title"));
        return e;
    }
}