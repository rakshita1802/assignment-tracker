package com.tracker.model;

public class Enrollment {
    private int    enrollmentId;
    private int    studentId;
    private int    assignmentId;
    private String status;
    private String studentName;
    private String assignmentTitle;

    public Enrollment() {}

    public Enrollment(int enrollmentId, int studentId,
                      int assignmentId, String status) {
        this.enrollmentId = enrollmentId;
        this.studentId    = studentId;
        this.assignmentId = assignmentId;
        this.status       = status;
    }

    public int    getEnrollmentId()                { return enrollmentId; }
    public void   setEnrollmentId(int id)          { this.enrollmentId = id; }
    public int    getStudentId()                   { return studentId; }
    public void   setStudentId(int id)             { this.studentId = id; }
    public int    getAssignmentId()                { return assignmentId; }
    public void   setAssignmentId(int id)          { this.assignmentId = id; }
    public String getStatus()                      { return status; }
    public void   setStatus(String s)              { this.status = s; }
    public String getStudentName()                 { return studentName; }
    public void   setStudentName(String n)         { this.studentName = n; }
    public String getAssignmentTitle()             { return assignmentTitle; }
    public void   setAssignmentTitle(String t)     { this.assignmentTitle = t; }
}