package com.tracker.dao;

import com.tracker.model.Submission;
import com.tracker.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for the 'submissions' table.
 * Handles file upload records — stores file path on disk, not the binary.
 */
public class SubmissionDAO {

    // ── CREATE / UPDATE ───────────────────────────────────────────
    /**
     * Inserts a new submission or replaces an existing one
     * (a student can re-submit to update their work).
     */
    public boolean saveSubmission(Submission s) {
        String sql = "INSERT INTO submissions " +
                "(student_id, assignment_id, file_name, file_path, student_comment) " +
                "VALUES (?,?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE " +
                "file_name=?, file_path=?, student_comment=?, submitted_at=NOW()";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1,    s.getStudentId());
            ps.setInt(2,    s.getAssignmentId());
            ps.setString(3, s.getFileName());
            ps.setString(4, s.getFilePath());
            ps.setString(5, s.getStudentComment());
            // ON DUPLICATE UPDATE values
            ps.setString(6, s.getFileName());
            ps.setString(7, s.getFilePath());
            ps.setString(8, s.getStudentComment());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[SubmissionDAO.save] " + e.getMessage());
            return false;
        }
    }

    // ── READ ──────────────────────────────────────────────────────
    /** Get a specific student's submission for one assignment. */
    public Submission getSubmission(int studentId, int assignmentId) {
        String sql = "SELECT s.*, u.name AS student_name, a.title AS assignment_title " +
                "FROM submissions s " +
                "JOIN users u ON s.student_id = u.user_id " +
                "JOIN assignments a ON s.assignment_id = a.assignment_id " +
                "WHERE s.student_id = ? AND s.assignment_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, assignmentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("[SubmissionDAO.get] " + e.getMessage());
        }
        return null;
    }

    /** All submissions for one assignment — teacher view. */
    public List<Submission> getSubmissionsByAssignment(int assignmentId) {
        List<Submission> list = new ArrayList<>();
        String sql = "SELECT s.*, u.name AS student_name, a.title AS assignment_title " +
                "FROM submissions s " +
                "JOIN users u ON s.student_id = u.user_id " +
                "JOIN assignments a ON s.assignment_id = a.assignment_id " +
                "WHERE s.assignment_id = ? ORDER BY s.submitted_at DESC";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, assignmentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[SubmissionDAO.getByAssignment] " + e.getMessage());
        }
        return list;
    }

    /** All submissions by one student — student history view. */
    public List<Submission> getSubmissionsByStudent(int studentId) {
        List<Submission> list = new ArrayList<>();
        String sql = "SELECT s.*, u.name AS student_name, a.title AS assignment_title " +
                "FROM submissions s " +
                "JOIN users u ON s.student_id = u.user_id " +
                "JOIN assignments a ON s.assignment_id = a.assignment_id " +
                "WHERE s.student_id = ? ORDER BY s.submitted_at DESC";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[SubmissionDAO.getByStudent] " + e.getMessage());
        }
        return list;
    }

    /** Count of submissions for an assignment (for teacher stats). */
    public int countSubmissions(int assignmentId) {
        String sql = "SELECT COUNT(*) FROM submissions WHERE assignment_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, assignmentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[SubmissionDAO.count] " + e.getMessage());
        }
        return 0;
    }

    // ── HELPER ───────────────────────────────────────────────────
    private Submission mapRow(ResultSet rs) throws SQLException {
        Submission s = new Submission();
        s.setSubmissionId(rs.getInt("submission_id"));
        s.setStudentId(rs.getInt("student_id"));
        s.setAssignmentId(rs.getInt("assignment_id"));
        s.setFileName(rs.getString("file_name"));
        s.setFilePath(rs.getString("file_path"));
        s.setStudentComment(rs.getString("student_comment"));
        s.setStudentName(rs.getString("student_name"));
        s.setAssignmentTitle(rs.getString("assignment_title"));
        Timestamp ts = rs.getTimestamp("submitted_at");
        if (ts != null) s.setSubmittedAt(ts.toLocalDateTime());
        return s;
    }
}