package com.tracker.model;

import java.time.LocalDateTime;

public class Assignment {
    private int           assignmentId;
    private String        title;
    private String        description;
    private String        subject;
    private LocalDateTime deadline;
    private int           createdBy;
    private String        createdByName;

    public Assignment() {}

    public Assignment(int assignmentId, String title, String description,
                      String subject, LocalDateTime deadline, int createdBy) {
        this.assignmentId = assignmentId;
        this.title        = title;
        this.description  = description;
        this.subject      = subject;
        this.deadline     = deadline;
        this.createdBy    = createdBy;
    }

    public int    getAssignmentId()              { return assignmentId; }
    public void   setAssignmentId(int id)        { this.assignmentId = id; }
    public String getTitle()                     { return title; }
    public void   setTitle(String t)             { this.title = t; }
    public String getDescription()               { return description; }
    public void   setDescription(String d)       { this.description = d; }
    public String getSubject()                   { return subject; }
    public void   setSubject(String s)           { this.subject = s; }
    public LocalDateTime getDeadline()           { return deadline; }
    public void   setDeadline(LocalDateTime dl)  { this.deadline = dl; }
    public int    getCreatedBy()                 { return createdBy; }
    public void   setCreatedBy(int id)           { this.createdBy = id; }
    public String getCreatedByName()             { return createdByName; }
    public void   setCreatedByName(String name)  { this.createdByName = name; }

    @Override
    public String toString() { return title + " (" + subject + ")"; }
}