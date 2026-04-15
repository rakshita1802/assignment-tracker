package com.tracker.model;

import java.time.LocalDateTime;

public class Notification {
    private int           notificationId;
    private int           studentId;
    private String        message;
    private LocalDateTime sentTime;
    private String        status;
    private String        studentName;

    public Notification() {}

    public Notification(int studentId, String message) {
        this.studentId = studentId;
        this.message   = message;
        this.sentTime  = LocalDateTime.now();
        this.status    = "Sent";
    }

    public int    getNotificationId()              { return notificationId; }
    public void   setNotificationId(int id)        { this.notificationId = id; }
    public int    getStudentId()                   { return studentId; }
    public void   setStudentId(int id)             { this.studentId = id; }
    public String getMessage()                     { return message; }
    public void   setMessage(String m)             { this.message = m; }
    public LocalDateTime getSentTime()             { return sentTime; }
    public void   setSentTime(LocalDateTime t)     { this.sentTime = t; }
    public String getStatus()                      { return status; }
    public void   setStatus(String s)              { this.status = s; }
    public String getStudentName()                 { return studentName; }
    public void   setStudentName(String n)         { this.studentName = n; }
}