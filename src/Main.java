package com.tracker;

import com.tracker.service.NotificationScheduler;
import com.tracker.ui.LoginFrame;
import com.tracker.util.DBConnection;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Could not set L&F: " + e.getMessage());
        }

        NotificationScheduler scheduler = new NotificationScheduler();
        scheduler.start();

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            scheduler.stop();
            DBConnection.closeConnection();
        }, "ShutdownHook"));
    }
}