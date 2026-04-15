package com.tracker.dao;

import com.tracker.model.Grade;
import com.tracker.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GradeDAO {

    public boolean addOrUpdateGrade(Grade g) {
        String sql = "INSERT INTO grades (student_id, assignment_id, marks, max_marks, feedback) " +
                "VALUES (?,?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE marks=?, max_marks=?, feedback=?, graded_at=NOW()";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, g.getStudentId());
            ps.setInt(2, g.getAssignmentId());
            ps.setInt(3, g.getMarks());
            ps.setInt(4, g.getMaxMarks());
            ps.setString(5, g.getFeedback());
            ps.setInt(6, g.getMarks());
            ps.setInt(7, g.getMaxMarks());
            ps.setString(8, g.getFeedback());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[GradeDAO.addOrUpdate] " + e.getMessage());
            return false;
        }
    }

    public List<Grade> getGradesByStudent(int studentId) {
        List<Grade> list = new ArrayList<>();
        String sql = "SELECT g.*, u.name AS student_name, a.title AS assignment_title " +
                "FROM grades g " +
                "JOIN users u ON g.student_id = u.user_id " +
                "JOIN assignments a ON g.assignment_id = a.assignment_id " +
                "WHERE g.student_id = ? ORDER BY g.graded_at DESC";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[GradeDAO.getByStudent] " + e.getMessage());
        }
        return list;
    }

    public List<Grade> getGradesByAssignment(int assignmentId) {
        List<Grade> list = new ArrayList<>();
        String sql = "SELECT g.*, u.name AS student_name, a.title AS assignment_title " +
                "FROM grades g " +
                "JOIN users u ON g.student_id = u.user_id " +
                "JOIN assignments a ON g.assignment_id = a.assignment_id " +
                "WHERE g.assignment_id = ? ORDER BY g.marks DESC";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, assignmentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[GradeDAO.getByAssignment] " + e.getMessage());
        }
        return list;
    }

    public Grade getGrade(int studentId, int assignmentId) {
        String sql = "SELECT g.*, u.name AS student_name, a.title AS assignment_title " +
                "FROM grades g " +
                "JOIN users u ON g.student_id = u.user_id " +
                "JOIN assignments a ON g.assignment_id = a.assignment_id " +
                "WHERE g.student_id = ? AND g.assignment_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, assignmentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("[GradeDAO.getGrade] " + e.getMessage());
        }
        return null;
    }

    // Stats for dashboard chart
    public double getAverageMarks(int assignmentId) {
        String sql = "SELECT AVG(marks * 100.0 / max_marks) FROM grades WHERE assignment_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, assignmentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("[GradeDAO.getAverage] " + e.getMessage());
        }
        return 0;
    }

    private Grade mapRow(ResultSet rs) throws SQLException {
        Grade g = new Grade(
                rs.getInt("student_id"),
                rs.getInt("assignment_id"),
                rs.getInt("marks"),
                rs.getInt("max_marks"),
                rs.getString("feedback")
        );
        g.setGradeId(rs.getInt("grade_id"));
        g.setStudentName(rs.getString("student_name"));
        g.setAssignmentTitle(rs.getString("assignment_title"));
        Timestamp ts = rs.getTimestamp("graded_at");
        if (ts != null) g.setGradedAt(ts.toLocalDateTime());
        return g;
    }
}