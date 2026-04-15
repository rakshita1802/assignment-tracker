package com.tracker.ui;

import com.tracker.dao.UserDAO;
import com.tracker.model.Role;
import com.tracker.model.User;
import com.tracker.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Login and Sign-Up frame.
 * Includes a visible "Forgot Password?" link that opens ForgotPasswordDialog.
 */
public class LoginFrame extends JFrame {

    private final UserDAO userDAO = new UserDAO();

    // Login fields
    private JTextField     emailField;
    private JPasswordField passwordField;

    // Sign-up fields
    private JTextField     suNameField;
    private JTextField     suEmailField;
    private JPasswordField suPasswordField;
    private JTextField     suPhoneField;
    private JComboBox<Role> suRoleCombo;

    public LoginFrame() {
        setTitle("Assignment Tracker — Login");
        setSize(880, 560);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new GridLayout(1, 2));
        root.add(buildBrandPanel());
        root.add(buildTabPanel());
        setContentPane(root);
    }

    // ── Brand panel (left side) ───────────────────────────────────
    private JPanel buildBrandPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(UIHelper.COLOR_SIDEBAR);

        JLabel icon = new JLabel("📚", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));

        JLabel title = new JLabel("Assignment Tracker");
        title.setFont(UIHelper.FONT_TITLE);
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Stay on top of every deadline");
        sub.setFont(UIHelper.FONT_BODY);
        sub.setForeground(UIHelper.COLOR_SIDEBAR_TEXT);

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        inner.add(icon);
        inner.add(Box.createVerticalStrut(16));
        inner.add(title);
        inner.add(Box.createVerticalStrut(8));
        inner.add(sub);
        p.add(inner);
        return p;
    }

    // ── Tabbed right panel ────────────────────────────────────────
    private JPanel buildTabPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UIHelper.COLOR_BG);
        p.setBorder(new EmptyBorder(40, 50, 40, 50));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UIHelper.FONT_BUTTON);
        tabs.addTab("Login",   buildLoginTab());
        tabs.addTab("Sign Up", buildSignUpTab());

        p.add(tabs, BorderLayout.CENTER);
        return p;
    }

    // ── LOGIN TAB ─────────────────────────────────────────────────
    private JPanel buildLoginTab() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(UIHelper.COLOR_BG);
        p.setBorder(new EmptyBorder(30, 0, 0, 0));

        emailField    = UIHelper.styledTextField();
        passwordField = UIHelper.styledPasswordField();

        emailField.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ── Email row ─────────────────────────────────────────────
        JLabel emailLbl = UIHelper.formLabel("Email Address");
        emailLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(emailLbl);
        p.add(Box.createVerticalStrut(6));
        p.add(emailField);
        p.add(Box.createVerticalStrut(16));

        // ── Password row ──────────────────────────────────────────
        JLabel passLbl = UIHelper.formLabel("Password");
        passLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(passLbl);
        p.add(Box.createVerticalStrut(6));
        p.add(passwordField);
        p.add(Box.createVerticalStrut(8));

        // ── Forgot Password link ──────────────────────────────────
        // Sits just below password field, left-aligned, clearly visible
        JButton forgotBtn = new JButton("Forgot Password?");
        forgotBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        forgotBtn.setForeground(UIHelper.COLOR_PRIMARY);
        forgotBtn.setBackground(UIHelper.COLOR_BG);
        forgotBtn.setBorderPainted(false);
        forgotBtn.setContentAreaFilled(false);
        forgotBtn.setFocusPainted(false);
        forgotBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgotBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        forgotBtn.setBorder(new EmptyBorder(0, 0, 0, 0));
        forgotBtn.addActionListener(e ->
                new ForgotPasswordDialog(this).setVisible(true));

        p.add(forgotBtn);
        p.add(Box.createVerticalStrut(20));

        // ── Login button ──────────────────────────────────────────
        JButton loginBtn = UIHelper.primaryButton("Login  →");
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.addActionListener(e -> handleLogin());
        // Allow Enter key to trigger login
        getRootPane().setDefaultButton(loginBtn);
        p.add(loginBtn);

        return p;
    }

    private void handleLogin() {
        String email    = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            UIHelper.showError(this, "Please enter both email and password.");
            return;
        }
        User user = userDAO.login(email, password);
        if (user == null) {
            UIHelper.showError(this, "Invalid email or password.");
            return;
        }
        dispose();
        launchDashboard(user);
    }

    private void launchDashboard(User user) {
        SwingUtilities.invokeLater(() -> {
            JFrame dashboard = switch (user.getRole()) {
                case Admin   -> new AdminDashboard(user);
                case Teacher -> new TeacherDashboard(user);
                default      -> new StudentDashboard(user);
            };
            dashboard.setVisible(true);
        });
    }

    // ── SIGN-UP TAB ───────────────────────────────────────────────
    private JPanel buildSignUpTab() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(UIHelper.COLOR_BG);
        p.setBorder(new EmptyBorder(20, 0, 0, 0));

        suNameField     = UIHelper.styledTextField();
        suEmailField    = UIHelper.styledTextField();
        suPasswordField = UIHelper.styledPasswordField();
        suPhoneField    = UIHelper.styledTextField();
        suRoleCombo     = new JComboBox<>(new Role[]{Role.Student, Role.Teacher});
        suRoleCombo.setFont(UIHelper.FONT_BODY);

        addFormRow(p, "Full Name",    suNameField);
        addFormRow(p, "Email",        suEmailField);
        addFormRow(p, "Password",     suPasswordField);
        addFormRow(p, "Phone Number", suPhoneField);
        addFormRow(p, "Register As",  suRoleCombo);
        p.add(Box.createVerticalStrut(20));

        JButton signUpBtn = UIHelper.successButton("Create Account");
        signUpBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        signUpBtn.addActionListener(e -> handleSignUp());
        p.add(signUpBtn);
        return p;
    }

    private void addFormRow(JPanel p, String label, JComponent field) {
        JLabel lbl = UIHelper.formLabel(label);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lbl);
        p.add(Box.createVerticalStrut(4));
        p.add(field);
        p.add(Box.createVerticalStrut(10));
    }

    private void handleSignUp() {
        String name  = suNameField.getText().trim();
        String email = suEmailField.getText().trim();
        String pass  = new String(suPasswordField.getPassword());
        String phone = suPhoneField.getText().trim();
        Role   role  = (Role) suRoleCombo.getSelectedItem();

        if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            UIHelper.showError(this, "Name, email, and password are required.");
            return;
        }
        if (pass.length() < 6) {
            UIHelper.showError(this, "Password must be at least 6 characters.");
            return;
        }
        User user = new User(0, name, email, pass, role, phone);
        if (userDAO.addUser(user)) {
            UIHelper.showSuccess(this,
                    "Account created! You can now log in.");
            suNameField.setText("");
            suEmailField.setText("");
            suPasswordField.setText("");
            suPhoneField.setText("");
        } else {
            UIHelper.showError(this,
                    "Sign-up failed. Email may already be in use.");
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}