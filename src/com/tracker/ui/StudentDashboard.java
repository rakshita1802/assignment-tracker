package com.tracker.ui;

import com.tracker.dao.AssignmentDAO;
import com.tracker.dao.EnrollmentDAO;
import com.tracker.dao.NotificationDAO;
import com.tracker.model.Assignment;
import com.tracker.model.Enrollment;
import com.tracker.model.Notification;
import com.tracker.model.User;
import com.tracker.util.UIHelper;
import com.tracker.util.ThemeManager;
import com.tracker.service.ExportService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Dashboard for the Student role.
 * Tabs: My Assignments | Notifications
 */
public class StudentDashboard extends JFrame {

    private final User           loggedIn;
    private final AssignmentDAO  assignDAO  = new AssignmentDAO();
    private final EnrollmentDAO  enrollDAO  = new EnrollmentDAO();
    private final NotificationDAO notifDAO  = new NotificationDAO();

    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private DefaultTableModel assignModel;
    private DefaultTableModel notifModel;

    // Keep track of assignment IDs matching table rows (for mark-complete)
    private List<Assignment> currentAssignments;

    public StudentDashboard(User loggedIn) {
        this.loggedIn = loggedIn;
        setTitle("Student Dashboard — " + loggedIn.getName());
        setSize(1000, 640);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIHelper.COLOR_BG);
        root.add(buildTopBar(), BorderLayout.NORTH);
        root.add(buildTabs(),   BorderLayout.CENTER);
        setContentPane(root);
    }

    // ── Top Bar ───────────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(14, 120, 87));
        bar.setBorder(new EmptyBorder(14, 24, 14, 24));

        JLabel title = new JLabel("🎒  Student Dashboard");
        title.setFont(UIHelper.FONT_HEADING);
        title.setForeground(Color.WHITE);

        JLabel welcome = new JLabel("Welcome, " + loggedIn.getName());
        welcome.setFont(UIHelper.FONT_SMALL);
        welcome.setForeground(new Color(180, 240, 210));

        JButton logout = UIHelper.dangerButton("Logout");
        logout.addActionListener(e -> { dispose(); new LoginFrame().setVisible(true); });

        bar.add(title,   BorderLayout.WEST);
        bar.add(welcome, BorderLayout.CENTER);
        bar.add(logout,  BorderLayout.EAST);
        JButton darkModeBtn = ThemeManager.createToggleButton(loggedIn.getUserId());
        bar.add(darkModeBtn, BorderLayout.CENTER);
        return bar;
    }

    // ── Tabs ──────────────────────────────────────────────────────
    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UIHelper.FONT_BUTTON);
        tabs.addTab("📋  My Assignments",  buildAssignmentsTab());
        tabs.addTab("🔔  Notifications",   buildNotificationsTab());
        tabs.addTab("📊  Analytics",  new DashboardCharts(loggedIn));
        tabs.addTab("📅  Calendar",   new CalendarPanel(loggedIn));
        tabs.addTab("📤  Export",     buildExportTab());
        return tabs;
    }

    private JPanel buildExportTab() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(30, 30, 30, 30));
        p.setBackground(UIHelper.COLOR_BG);

        JLabel title = new JLabel("Export My Data");
        title.setFont(UIHelper.FONT_HEADING);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(title);
        p.add(Box.createVerticalStrut(20));

        ExportService exportService = new ExportService();
        AssignmentDAO aDao = new AssignmentDAO();

        JButton excelBtn = UIHelper.successButton("📊 Export Assignments to Excel");
        excelBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        excelBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new java.io.File("my_assignments.xlsx"));
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                boolean ok = exportService.exportAssignmentsToExcel(
                        aDao.getAssignmentsByStudent(loggedIn.getUserId()),
                        fc.getSelectedFile().getAbsolutePath());
                if (ok) UIHelper.showSuccess(this, "Excel exported successfully!");
                else    UIHelper.showError(this, "Export failed. Try again.");
            }
        });

        JButton pdfBtn = UIHelper.primaryButton("📄 Export Assignments to PDF");
        pdfBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        pdfBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new java.io.File("my_assignments.pdf"));
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                boolean ok = exportService.exportAssignmentsToPdf(
                        aDao.getAssignmentsByStudent(loggedIn.getUserId()),
                        fc.getSelectedFile().getAbsolutePath());
                if (ok) UIHelper.showSuccess(this, "PDF exported successfully!");
                else    UIHelper.showError(this, "Export failed. Try again.");
            }
        });

        p.add(excelBtn);
        p.add(Box.createVerticalStrut(12));
        p.add(pdfBtn);
        return p;
    }

    // ──────────────────────────────────────────────────────────────
    // TAB 1 – MY ASSIGNMENTS
    // ──────────────────────────────────────────────────────────────
    private JPanel buildAssignmentsTab() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBorder(new EmptyBorder(16, 16, 16, 16));
        p.setBackground(UIHelper.COLOR_BG);

        // Summary bar (stats cards)
        JPanel statsBar = buildStatsBar();

        // Toolbar
        JPanel tb = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tb.setOpaque(false);

        JComboBox<String> filterCombo = new JComboBox<>(
                new String[]{"All", "Pending", "Completed"});
        filterCombo.setFont(UIHelper.FONT_BODY);
        JButton markBtn    = UIHelper.successButton("✔ Mark as Completed");
        JButton refreshBtn = UIHelper.outlineButton("⟳ Refresh");

        tb.add(new JLabel("Filter: "));
        tb.add(filterCombo);
        tb.add(markBtn);
        tb.add(refreshBtn);

        // Table
        String[] cols = {"#","Title","Subject","Deadline","Teacher","Status","Days Left"};
        assignModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(assignModel);
        UIHelper.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.getColumnModel().getColumn(6).setMaxWidth(80);

        // Color-code status column
        table.getColumnModel().getColumn(5).setCellRenderer(
                new javax.swing.table.DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(
                            JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                        super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                        setBorder(new EmptyBorder(0, 10, 0, 10));
                        if (!sel) {
                            String status = val == null ? "" : val.toString();
                            if ("Completed".equals(status)) {
                                setForeground(UIHelper.COLOR_SUCCESS);
                            } else {
                                // Pending — check urgency via Days Left col
                                try {
                                    int days = Integer.parseInt(
                                            t.getValueAt(row, 6).toString().replace("d","").trim());
                                    setForeground(days <= 1 ? UIHelper.COLOR_DANGER :
                                            days <= 3 ? new Color(200, 120, 0) :
                                                    UIHelper.COLOR_TEXT);
                                } catch (Exception ex) {
                                    setForeground(UIHelper.COLOR_TEXT);
                                }
                            }
                        }
                        return this;
                    }
                }
        );

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(220,225,235)));

        // Load & filter
        loadAssignments("All");
        filterCombo.addActionListener(e ->
                loadAssignments((String) filterCombo.getSelectedItem()));

        markBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { UIHelper.showError(this,"Select an assignment to mark."); return; }
            String status = (String) assignModel.getValueAt(row, 5);
            if ("Completed".equals(status)) {
                UIHelper.showError(this,"Already marked as completed."); return;
            }
            int assignId = currentAssignments.get(row).getAssignmentId();
            if (enrollDAO.markCompleted(loggedIn.getUserId(), assignId)) {
                loadAssignments((String) filterCombo.getSelectedItem());
                UIHelper.showSuccess(this,"Assignment marked as completed!");
            } else {
                UIHelper.showError(this,"Could not update. Try again.");
            }
        });

        refreshBtn.addActionListener(e ->
                loadAssignments((String) filterCombo.getSelectedItem()));

        JPanel topSection = new JPanel(new BorderLayout(0, 8));
        topSection.setOpaque(false);
        topSection.add(statsBar, BorderLayout.NORTH);
        topSection.add(tb,       BorderLayout.SOUTH);

        p.add(topSection, BorderLayout.NORTH);
        p.add(sp,         BorderLayout.CENTER);
        return p;
    }

    /** Three summary stat cards at the top. */
    private JPanel buildStatsBar() {
        JPanel bar = new JPanel(new GridLayout(1, 3, 12, 0));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(0, 0, 8, 0));

        List<Assignment>  all       = assignDAO.getAssignmentsByStudent(loggedIn.getUserId());
        List<Enrollment>  enrolls   = enrollDAO.getEnrollmentsByStudent(loggedIn.getUserId());
        long total     = all.size();
        long completed = enrolls.stream().filter(e -> "Completed".equals(e.getStatus())).count();
        long pending   = total - completed;

        bar.add(statCard("Total Assignments", String.valueOf(total),  UIHelper.COLOR_PRIMARY));
        bar.add(statCard("Pending",           String.valueOf(pending), UIHelper.COLOR_DANGER));
        bar.add(statCard("Completed",         String.valueOf(completed), UIHelper.COLOR_SUCCESS));
        return bar;
    }

    private JPanel statCard(String label, String value, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(UIHelper.COLOR_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
                new EmptyBorder(12, 16, 12, 16)
        ));
        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 28));
        val.setForeground(accent);
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIHelper.FONT_SMALL);
        lbl.setForeground(UIHelper.COLOR_TEXT_MUTED);
        card.add(val, BorderLayout.CENTER);
        card.add(lbl, BorderLayout.SOUTH);
        return card;
    }

    private void loadAssignments(String filter) {
        assignModel.setRowCount(0);
        currentAssignments = assignDAO.getAssignmentsByStudent(loggedIn.getUserId());
        List<Enrollment> enrollments = enrollDAO.getEnrollmentsByStudent(loggedIn.getUserId());

        // Build a quick lookup: assignmentId -> status
        java.util.Map<Integer,String> statusMap = new java.util.HashMap<>();
        for (Enrollment e : enrollments) {
            statusMap.put(e.getAssignmentId(), e.getStatus());
        }

        int idx = 1;
        for (Assignment a : currentAssignments) {
            String status   = statusMap.getOrDefault(a.getAssignmentId(), "Pending");
            if (!"All".equals(filter) && !filter.equals(status)) continue;

            long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(
                    LocalDateTime.now(), a.getDeadline());

            assignModel.addRow(new Object[]{
                    idx++,
                    a.getTitle(),
                    a.getSubject(),
                    a.getDeadline().format(DT_FMT),
                    a.getCreatedByName(),
                    status,
                    daysLeft + "d"
            });
        }
    }

    // ──────────────────────────────────────────────────────────────
    // TAB 2 – NOTIFICATIONS
    // ──────────────────────────────────────────────────────────────
    private JPanel buildNotificationsTab() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBorder(new EmptyBorder(16, 16, 16, 16));
        p.setBackground(UIHelper.COLOR_BG);

        String[] cols = {"#","Message","Received At","Status"};
        notifModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(notifModel);
        UIHelper.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(420);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(220,225,235)));

        JPanel tb = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tb.setOpaque(false);

        JButton markReadBtn = UIHelper.outlineButton("Mark as Read");
        JButton refreshBtn  = UIHelper.outlineButton("⟳ Refresh");
        tb.add(markReadBtn);
        tb.add(refreshBtn);

        // Notification list (in memory for mark-read tracking)
        final List<Notification>[] notifList = new List[]{null};

        Runnable loadFn = () -> {
            notifModel.setRowCount(0);
            notifList[0] = notifDAO.getNotificationsForStudent(loggedIn.getUserId());
            int i = 1;
            for (Notification n : notifList[0]) {
                notifModel.addRow(new Object[]{
                        i++,
                        n.getMessage(),
                        n.getSentTime().format(DT_FMT),
                        n.getStatus()
                });
            }
        };

        loadFn.run();

        markReadBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { UIHelper.showError(this,"Select a notification."); return; }
            Notification n = notifList[0].get(row);
            if ("Read".equals(n.getStatus())) { return; }
            notifDAO.markRead(n.getNotificationId());
            loadFn.run();
        });

        refreshBtn.addActionListener(e -> loadFn.run());

        p.add(tb, BorderLayout.NORTH);
        p.add(sp, BorderLayout.CENTER);
        return p;
    }
}