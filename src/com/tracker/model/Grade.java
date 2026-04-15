package com.tracker.model;

import java.time.LocalDateTime;

public class Grade {
    private int           gradeId;
    private int           studentId;
    private int           assignmentId;
    private int           marks;
    private int           maxMarks;
    private String        feedback;
    private LocalDateTime gradedAt;

    // Display fields
    private String studentName;
    private String assignmentTitle;

    public Grade() {}

    public Grade(int studentId, int assignmentId, int marks, int maxMarks, String feedback) {
        this.studentId    = studentId;
        this.assignmentId = assignmentId;
        this.marks        = marks;
        this.maxMarks     = maxMarks;
        this.feedback     = feedback;
    }

    public int    getGradeId()                 { return gradeId; }
    public void   setGradeId(int id)           { this.gradeId = id; }
    public int    getStudentId()               { return studentId; }
    public void   setStudentId(int id)         { this.studentId = id; }
    public int    getAssignmentId()            { return assignmentId; }
    public void   setAssignmentId(int id)      { this.assignmentId = id; }
    public int    getMarks()                   { return marks; }
    public void   setMarks(int m)              { this.marks = m; }
    public int    getMaxMarks()                { return maxMarks; }
    public void   setMaxMarks(int m)           { this.maxMarks = m; }
    public String getFeedback()                { return feedback; }
    public void   setFeedback(String f)        { this.feedback = f; }
    public LocalDateTime getGradedAt()         { return gradedAt; }
    public void   setGradedAt(LocalDateTime t) { this.gradedAt = t; }
    public String getStudentName()             { return studentName; }
    public void   setStudentName(String n)     { this.studentName = n; }
    public String getAssignmentTitle()         { return assignmentTitle; }
    public void   setAssignmentTitle(String t) { this.assignmentTitle = t; }

    public String getGradeLetter() {
        double pct = (marks * 100.0) / maxMarks;
        if (pct >= 90) return "A+";
        if (pct >= 80) return "A";
        if (pct >= 70) return "B";
        if (pct >= 60) return "C";
        if (pct >= 50) return "D";
        return "F";
    }
}