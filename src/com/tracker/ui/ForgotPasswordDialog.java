package com.tracker.ui;

import com.tracker.dao.UserDAO;
import com.tracker.service.EmailService;
import com.tracker.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Random;

/**
 * Two-step forgot-password dialog:
 * Step 1 — Enter email → OTP sent
 * Step 2 — Enter OTP + new password → password reset
 */
public class ForgotPasswordDialog extends JDialog {

    private final UserDAO      userDAO      = new UserDAO();
    private final EmailService emailService = new EmailService();

    private JTextField  emailField;
    private JTextField  otpField;
    private JPasswordField newPassField;
    private JPasswordField confirmPassField;

    private JPanel step1Panel;
    private JPanel step2Panel;

    private String generatedOtp;
    private String currentEmail;

    public ForgotPasswordDialog(Frame parent) {
        super(parent, "Forgot Password", true);
        setSize(420, 320);
        setLocationRelativeTo(parent);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new CardLayout());
        root.setBackground(UIHelper.COLOR_BG);

        step1Panel = buildStep1();
        step2Panel = buildStep2();

        root.add(step1Panel, "step1");
        root.add(step2Panel, "step2");

        setContentPane(root);
    }

    // ── Step 1: Enter email ───────────────────────────────────────
    private JPanel buildStep1() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(UIHelper.COLOR_BG);
        p.setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel title = new JLabel("Forgot Password");
        title.setFont(UIHelper.FONT_HEADING);
        title.setForeground(UIHelper.COLOR_TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Enter your registered email to receive an OTP.");
        sub.setFont(UIHelper.FONT_SMALL);
        sub.setForeground(UIHelper.COLOR_TEXT_MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        emailField = UIHelper.styledTextField();
        emailField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton sendBtn = UIHelper.primaryButton("Send OTP →");
        sendBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        sendBtn.addActionListener(e -> handleSendOtp());

        p.add(title);
        p.add(Box.createVerticalStrut(6));
        p.add(sub);
        p.add(Box.createVerticalStrut(20));
        p.add(UIHelper.formLabel("Email Address"));
        p.add(Box.createVerticalStrut(6));
        p.add(emailField);
        p.add(Box.createVerticalStrut(20));
        p.add(sendBtn);
        return p;
    }

    // ── Step 2: Enter OTP + new password ─────────────────────────
    private JPanel buildStep2() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(UIHelper.COLOR_BG);
        p.setBorder(new EmptyBorder(24, 30, 24, 30));

        JLabel title = new JLabel("Enter OTP & New Password");
        title.setFont(UIHelper.FONT_HEADING);
        title.setForeground(UIHelper.COLOR_TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        otpField       = UIHelper.styledTextField();
        newPassField    = UIHelper.styledPasswordField();
        confirmPassField= UIHelper.styledPasswordField();

        otpField.setAlignmentX(Component.LEFT_ALIGNMENT);
        newPassField.setAlignmentX(Component.LEFT_ALIGNMENT);
        confirmPassField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton resetBtn = UIHelper.successButton("Reset Password");
        resetBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        resetBtn.addActionListener(e -> handleResetPassword());

        JButton backBtn = UIHelper.outlineButton("← Back");
        backBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        backBtn.addActionListener(e -> showStep(1));

        p.add(title);
        p.add(Box.createVerticalStrut(16));
        p.add(UIHelper.formLabel("OTP Code (check your email)"));
        p.add(Box.createVerticalStrut(4));
        p.add(otpField);
        p.add(Box.createVerticalStrut(10));
        p.add(UIHelper.formLabel("New Password"));
        p.add(Box.createVerticalStrut(4));
        p.add(newPassField);
        p.add(Box.createVerticalStrut(10));
        p.add(UIHelper.formLabel("Confirm Password"));
        p.add(Box.createVerticalStrut(4));
        p.add(confirmPassField);
        p.add(Box.createVerticalStrut(16));
        p.add(resetBtn);
        p.add(Box.createVerticalStrut(8));
        p.add(backBtn);
        return p;
    }

    // ── Handlers ──────────────────────────────────────────────────
    private void handleSendOtp() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            UIHelper.showError(this, "Please enter your email.");
            return;
        }

        // Check email exists
        if (userDAO.getUsersByRole(com.tracker.model.Role.Student).stream()
                .noneMatch(u -> u.getEmail().equals(email)) &&
                userDAO.getUsersByRole(com.tracker.model.Role.Teacher).stream()
                        .noneMatch(u -> u.getEmail().equals(email)) &&
                userDAO.getUsersByRole(com.tracker.model.Role.Admin).stream()
                        .noneMatch(u -> u.getEmail().equals(email))) {
            UIHelper.showError(this, "Email not found in the system.");
            return;
        }

        // Generate 6-digit OTP
        generatedOtp = String.valueOf(100000 + new Random().nextInt(900000));
        currentEmail = email;

        // Save OTP to DB
        saveOtpToDB(email, generatedOtp);

        // Send email
        boolean sent = emailService.sendEmail(
                email, "User",
                "Your Assignment Tracker OTP",
                "Your OTP for password reset is: <strong>" + generatedOtp +
                        "</strong><br>This OTP expires in 10 minutes."
        );

        if (sent) {
            UIHelper.showSuccess(this,
                    "OTP sent to " + email + ". Check your inbox.");
            showStep(2);
        } else {
            // Even if email fails, show OTP in console for testing
            System.out.println("[ForgotPassword] OTP for " + email + " : " + generatedOtp);
            UIHelper.showSuccess(this,
                    "OTP generated (check console if email failed): " + generatedOtp);
            showStep(2);
        }
    }

    private void handleResetPassword() {
        String otp      = otpField.getText().trim();
        String newPass  = new String(newPassField.getPassword());
        String confirm  = new String(confirmPassField.getPassword());

        if (otp.isEmpty() || newPass.isEmpty()) {
            UIHelper.showError(this, "Please fill in all fields.");
            return;
        }
        if (!newPass.equals(confirm)) {
            UIHelper.showError(this, "Passwords do not match.");
            return;
        }
        if (newPass.length() < 6) {
            UIHelper.showError(this, "Password must be at least 6 characters.");
            return;
        }
        if (!otp.equals(generatedOtp)) {
            UIHelper.showError(this, "Invalid OTP. Please try again.");
            return;
        }
        if (!isOtpValid(currentEmail, otp)) {
            UIHelper.showError(this, "OTP has expired. Please request a new one.");
            return;
        }

        if (userDAO.updatePassword(currentEmail, newPass)) {
            markOtpUsed(currentEmail, otp);
            UIHelper.showSuccess(this, "Password reset successfully! You can now login.");
            dispose();
        } else {
            UIHelper.showError(this, "Failed to reset password. Try again.");
        }
    }

    private void showStep(int step) {
        CardLayout cl = (CardLayout) getContentPane().getLayout();
        cl.show(getContentPane(), "step" + step);
    }

    // ── OTP DB helpers ────────────────────────────────────────────
    private void saveOtpToDB(String email, String otp) {
        String sql = "INSERT INTO password_otp (email, otp_code, expires_at) " +
                "VALUES (?, ?, ?)";
        try (PreparedStatement ps =
                     com.tracker.util.DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, otp);
            ps.setTimestamp(3, Timestamp.valueOf(
                    LocalDateTime.now().plusMinutes(10)));
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[ForgotPassword.saveOtp] " + e.getMessage());
        }
    }

    private boolean isOtpValid(String email, String otp) {
        String sql = "SELECT COUNT(*) FROM password_otp " +
                "WHERE email=? AND otp_code=? AND used=FALSE " +
                "AND expires_at > NOW()";
        try (PreparedStatement ps =
                     com.tracker.util.DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, otp);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("[ForgotPassword.isValid] " + e.getMessage());
            return false;
        }
    }

    private void markOtpUsed(String email, String otp) {
        String sql = "UPDATE password_otp SET used=TRUE WHERE email=? AND otp_code=?";
        try (PreparedStatement ps =
                     com.tracker.util.DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, otp);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[ForgotPassword.markUsed] " + e.getMessage());
        }
    }
}