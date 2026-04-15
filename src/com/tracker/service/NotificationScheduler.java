package com.tracker.service;

import com.tracker.dao.AssignmentDAO;
import com.tracker.dao.EnrollmentDAO;
import com.tracker.dao.NotificationDAO;
import com.tracker.dao.UserDAO;
import com.tracker.model.Assignment;
import com.tracker.model.Enrollment;
import com.tracker.model.Notification;
import com.tracker.model.User;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NotificationScheduler {

    private static final int CHECK_INTERVAL_SECONDS = 60;   // runs every 60 seconds
    private static final int ALERT_HOURS_BEFORE     = 24;   // alert 24 hours before deadline

    private final ScheduledExecutorService executor;
    private final AssignmentDAO   assignmentDAO   = new AssignmentDAO();
    private final EnrollmentDAO   enrollmentDAO   = new EnrollmentDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final UserDAO         userDAO         = new UserDAO();
    private final EmailService    emailService    = new EmailService();

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public NotificationScheduler() {
        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "NotificationScheduler-Thread");
            t.setDaemon(true);
            return t;
        });
    }

    public void start() {
        executor.scheduleAtFixedRate(
                this::checkAndNotify, 0, CHECK_INTERVAL_SECONDS, TimeUnit.SECONDS);
        System.out.println("[Scheduler] Started. Checking every "
                + CHECK_INTERVAL_SECONDS + " seconds.");
    }

    public void stop() {
        executor.shutdown();
        System.out.println("[Scheduler] Stopped.");
    }

    // ── Core logic ────────────────────────────────────────────────
    private void checkAndNotify() {
        System.out.println("[Scheduler] Checking upcoming deadlines...");

        List<Assignment> upcoming =
                assignmentDAO.getUpcomingDeadlines(ALERT_HOURS_BEFORE);

        if (upcoming.isEmpty()) {
            System.out.println("[Scheduler] No upcoming deadlines.");
            return;
        }

        for (Assignment a : upcoming) {
            List<Enrollment> enrollments =
                    enrollmentDAO.getEnrollmentsByAssignment(a.getAssignmentId());

            for (Enrollment enr : enrollments) {
                if (!"Pending".equals(enr.getStatus())) continue;

                // Fetch full student details (need email)
                User student = userDAO.getUserById(enr.getStudentId());
                if (student == null) continue;

                String message = buildMessage(student.getName(), a);

                // 1. Save in-app notification to DB
                Notification notif = new Notification(student.getUserId(), message);
                notificationDAO.addNotification(notif);

                // 2. Send email notification
                boolean sent = emailService.sendEmail(
                        student.getEmail(),
                        student.getName(),
                        "⏰ Deadline Reminder: " + a.getTitle(),
                        message
                );

                // 3. Simulate SMS in console
                printSmsSimulation(student.getName(), student.getPhoneNumber(), message);

                System.out.println("[Scheduler] Notified " + student.getName()
                        + " | Email: " + (sent ? "✓ sent" : "✗ failed"));
            }
        }
    }

    private String buildMessage(String studentName, Assignment a) {
        return String.format(
                "Your assignment '%s' (Subject: %s) is due on %s. Please submit on time!",
                a.getTitle(),
                a.getSubject(),
                a.getDeadline().format(FMT)
        );
    }

    private void printSmsSimulation(String name, String phone, String message) {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("[SMS → " + name + " | " + phone + "]");
        System.out.println(message);
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
}