package com.tracker.model;

import java.time.LocalDateTime;

/**
 * Represents a student's file submission for an assignment.
 * Stored in the 'submissions' table.
 */
public class Submission {
    private int           submissionId;
    private int           studentId;
    private int           assignmentId;
    private String        fileName;       // original file name shown to teacher
    private String        filePath;       // absolute path on disk
    private String        studentComment; // optional note from student
    private LocalDateTime submittedAt;

    // Display fields (populated via JOIN)
    private String studentName;
    private String assignmentTitle;

    public Submission() {}

    // ── Getters & Setters ─────────────────────────────────────────
    public int    getSubmissionId()                { return submissionId; }
    public void   setSubmissionId(int id)          { this.submissionId = id; }
    public int    getStudentId()                   { return studentId; }
    public void   setStudentId(int id)             { this.studentId = id; }
    public int    getAssignmentId()                { return assignmentId; }
    public void   setAssignmentId(int id)          { this.assignmentId = id; }
    public String getFileName()                    { return fileName; }
    public void   setFileName(String n)            { this.fileName = n; }
    public String getFilePath()                    { return filePath; }
    public void   setFilePath(String p)            { this.filePath = p; }
    public String getStudentComment()              { return studentComment; }
    public void   setStudentComment(String c)      { this.studentComment = c; }
    public LocalDateTime getSubmittedAt()          { return submittedAt; }
    public void   setSubmittedAt(LocalDateTime t)  { this.submittedAt = t; }
    public String getStudentName()                 { return studentName; }
    public void   setStudentName(String n)         { this.studentName = n; }
    public String getAssignmentTitle()             { return assignmentTitle; }
    public void   setAssignmentTitle(String t)     { this.assignmentTitle = t; }
}