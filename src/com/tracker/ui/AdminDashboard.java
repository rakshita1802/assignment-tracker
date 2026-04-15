package com.tracker.ui;

import com.tracker.dao.AssignmentDAO;
import com.tracker.dao.NotificationDAO;
import com.tracker.dao.UserDAO;
import com.tracker.model.*;
import com.tracker.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Dashboard for the Admin role.
 * Tabs: Users | Assignments | Notifications
 */
public class AdminDashboard extends JFrame {

    private final User         loggedIn;
    private final UserDAO      userDAO      = new UserDAO();
    private final AssignmentDAO assignDAO   = new AssignmentDAO();
    private final NotificationDAO notifDAO  = new NotificationDAO();

    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    // Table models
    private DefaultTableModel usersModel;
    private DefaultTableModel assignModel;
    private DefaultTableModel notifModel;

    public AdminDashboard(User loggedIn) {
        this.loggedIn = loggedIn;
        setTitle("Admin Dashboard — " + loggedIn.getName());
        setSize(1100, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
    }

    // ── UI Construction ──────────────────────────────────────────
    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIHelper.COLOR_BG);

        root.add(buildTopBar(), BorderLayout.NORTH);
        root.add(buildTabs(),   BorderLayout.CENTER);

        setContentPane(root);
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UIHelper.COLOR_SIDEBAR);
        bar.setBorder(new EmptyBorder(14, 24, 14, 24));

        JLabel title = new JLabel("⚙  Admin Control Panel");
        title.setFont(UIHelper.FONT_HEADING);
        title.setForeground(Color.WHITE);

        JLabel user = new JLabel("Logged in as: " + loggedIn.getName());
        user.setFont(UIHelper.FONT_SMALL);
        user.setForeground(UIHelper.COLOR_SIDEBAR_TEXT);

        JButton logout = UIHelper.dangerButton("Logout");
        logout.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        bar.add(title, BorderLayout.WEST);
        bar.add(user,  BorderLayout.CENTER);
        bar.add(logout, BorderLayout.EAST);
        return bar;
    }

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UIHelper.FONT_BUTTON);
        tabs.addTab("👥  Users",        buildUsersPanel());
        tabs.addTab("📋  Assignments",  buildAssignmentsPanel());
        tabs.addTab("🔔  Notifications",buildNotificationsPanel());
        return tabs;
    }

    // ──────────────────────────────────────────────────────────────
    // TAB 1: USERS
    // ──────────────────────────────────────────────────────────────
    private JPanel buildUsersPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBorder(new EmptyBorder(16, 16, 16, 16));
        p.setBackground(UIHelper.COLOR_BG);

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        JButton addBtn    = UIHelper.primaryButton("+ Add User");
        JButton editBtn   = UIHelper.outlineButton("Edit");
        JButton deleteBtn = UIHelper.dangerButton("Delete");
        JButton refreshBtn= UIHelper.outlineButton("⟳ Refresh");
        toolbar.add(addBtn);
        toolbar.add(editBtn);
        toolbar.add(deleteBtn);
        toolbar.add(refreshBtn);

        // Table
        String[] cols = {"ID","Name","Email","Role","Phone"};
        usersModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(usersModel);
        UIHelper.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(220,225,235)));

        loadUsers();

        // Listeners
        addBtn.addActionListener(e -> openUserForm(null, table));
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { UIHelper.showError(this,"Select a user first."); return; }
            int id = (int) usersModel.getValueAt(row, 0);
            openUserForm(userDAO.getUserById(id), table);
        });
        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { UIHelper.showError(this,"Select a user first."); return; }
            int id = (int) usersModel.getValueAt(row, 0);
            if (UIHelper.confirm(this, "Delete this user?")) {
                userDAO.deleteUser(id);
                loadUsers();
            }
        });
        refreshBtn.addActionListener(e -> loadUsers());

        p.add(toolbar, BorderLayout.NORTH);
        p.add(sp,       BorderLayout.CENTER);
        return p;
    }

    private void loadUsers() {
        usersModel.setRowCount(0);
        for (User u : userDAO.getAllUsers()) {
            usersModel.addRow(new Object[]{
                    u.getUserId(), u.getName(), u.getEmail(), u.getRole(), u.getPhoneNumber()
            });
        }
    }

    private void openUserForm(User existing, JTable table) {
        JDialog dlg = new JDialog(this, existing == null ? "Add User" : "Edit User", true);
        dlg.setSize(380, 400);
        dlg.setLocationRelativeTo(this);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));
        form.setBackground(UIHelper.COLOR_BG);

        JTextField nameF  = UIHelper.styledTextField();
        JTextField emailF = UIHelper.styledTextField();
        JPasswordField passF = UIHelper.styledPasswordField();
        JTextField phoneF = UIHelper.styledTextField();
        JComboBox<Role> roleC = new JComboBox<>(Role.values());
        roleC.setFont(UIHelper.FONT_BODY);

        if (existing != null) {
            nameF.setText(existing.getName());
            emailF.setText(existing.getEmail());
            passF.setText(existing.getPassword());
            phoneF.setText(existing.getPhoneNumber());
            roleC.setSelectedItem(existing.getRole());
        }

        addFormRow2(form, "Name",     nameF);
        addFormRow2(form, "Email",    emailF);
        addFormRow2(form, "Password", passF);
        addFormRow2(form, "Phone",    phoneF);
        addFormRow2(form, "Role",     roleC);
        form.add(Box.createVerticalStrut(16));

        JButton save = UIHelper.primaryButton(existing == null ? "Create" : "Update");
        save.setAlignmentX(Component.LEFT_ALIGNMENT);
        save.addActionListener(e -> {
            String name  = nameF.getText().trim();
            String email = emailF.getText().trim();
            String pass  = new String(passF.getPassword());
            String phone = phoneF.getText().trim();
            Role   role  = (Role) roleC.getSelectedItem();
            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                UIHelper.showError(dlg, "Name, email, password required."); return;
            }
            if (existing == null) {
                userDAO.addUser(new User(0, name, email, pass, role, phone));
            } else {
                existing.setName(name); existing.setEmail(email);
                existing.setPassword(pass); existing.setRole(role);
                existing.setPhoneNumber(phone);
                userDAO.updateUser(existing);
            }
            loadUsers();
            dlg.dispose();
        });
        form.add(save);

        dlg.setContentPane(form);
        dlg.setVisible(true);
    }

    private void addFormRow2(JPanel p, String label, JComponent f) {
        p.add(UIHelper.formLabel(label));
        p.add(Box.createVerticalStrut(4));
        p.add(f);
        p.add(Box.createVerticalStrut(10));
    }

    // ──────────────────────────────────────────────────────────────
    // TAB 2: ASSIGNMENTS
    // ──────────────────────────────────────────────────────────────
    private JPanel buildAssignmentsPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBorder(new EmptyBorder(16, 16, 16, 16));
        p.setBackground(UIHelper.COLOR_BG);

        String[] cols = {"ID","Title","Subject","Deadline","Teacher"};
        assignModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(assignModel);
        UIHelper.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(220,225,235)));

        JButton refreshBtn = UIHelper.outlineButton("⟳ Refresh");
        refreshBtn.addActionListener(e -> loadAssignments());

        loadAssignments();

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.setOpaque(false);
        toolbar.add(refreshBtn);

        p.add(toolbar, BorderLayout.NORTH);
        p.add(sp,       BorderLayout.CENTER);
        return p;
    }

    private void loadAssignments() {
        assignModel.setRowCount(0);
        for (Assignment a : assignDAO.getAllAssignments()) {
            assignModel.addRow(new Object[]{
                    a.getAssignmentId(), a.getTitle(), a.getSubject(),
                    a.getDeadline().format(DT_FMT), a.getCreatedByName()
            });
        }
    }

    // ──────────────────────────────────────────────────────────────
    // TAB 3: NOTIFICATIONS
    // ──────────────────────────────────────────────────────────────
    private JPanel buildNotificationsPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBorder(new EmptyBorder(16, 16, 16, 16));
        p.setBackground(UIHelper.COLOR_BG);

        String[] cols = {"ID","Student","Message","Sent At","Status"};
        notifModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(notifModel);
        UIHelper.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(2).setPreferredWidth(340);
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(220,225,235)));

        JButton refreshBtn = UIHelper.outlineButton("⟳ Refresh");
        refreshBtn.addActionListener(e -> loadNotifications());
        loadNotifications();

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.setOpaque(false);
        toolbar.add(refreshBtn);

        p.add(toolbar, BorderLayout.NORTH);
        p.add(sp,       BorderLayout.CENTER);
        return p;
    }

    private void loadNotifications() {
        notifModel.setRowCount(0);
        for (Notification n : notifDAO.getAllNotifications()) {
            notifModel.addRow(new Object[]{
                    n.getNotificationId(), n.getStudentName(),
                    n.getMessage(), n.getSentTime().format(DT_FMT), n.getStatus()
            });
        }
    }
}