package com.tracker.ui;

import com.tracker.dao.AssignmentDAO;
import com.tracker.dao.EnrollmentDAO;
import com.tracker.dao.UserDAO;
import com.tracker.model.Assignment;
import com.tracker.model.Enrollment;
import com.tracker.model.Role;
import com.tracker.model.User;
import com.tracker.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Dashboard for the Teacher role.
 * Tabs: My Assignments | Assign to Students | Enrolled Students
 */
public class TeacherDashboard extends JFrame {

    private final User          loggedIn;
    private final AssignmentDAO assignDAO  = new AssignmentDAO();
    private final EnrollmentDAO enrollDAO  = new EnrollmentDAO();
    private final UserDAO       userDAO    = new UserDAO();

    private static final DateTimeFormatter DT_FMT  =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private static final DateTimeFormatter DT_INPUT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private DefaultTableModel assignModel;
    private DefaultTableModel enrollModel;

    public TeacherDashboard(User loggedIn) {
        this.loggedIn = loggedIn;
        setTitle("Teacher Dashboard — " + loggedIn.getName());
        setSize(1100, 680);
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

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(20, 80, 150));
        bar.setBorder(new EmptyBorder(14, 24, 14, 24));

        JLabel title = new JLabel("🎓  Teacher Dashboard");
        title.setFont(UIHelper.FONT_HEADING);
        title.setForeground(Color.WHITE);

        JLabel user = new JLabel("Welcome, " + loggedIn.getName());
        user.setFont(UIHelper.FONT_SMALL);
        user.setForeground(UIHelper.COLOR_SIDEBAR_TEXT);

        JButton logout = UIHelper.dangerButton("Logout");
        logout.addActionListener(e -> { dispose(); new LoginFrame().setVisible(true); });

        bar.add(title,  BorderLayout.WEST);
        bar.add(user,   BorderLayout.CENTER);
        bar.add(logout, BorderLayout.EAST);
        return bar;
    }

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UIHelper.FONT_BUTTON);
        tabs.addTab("📋  My Assignments",     buildAssignmentsTab());
        tabs.addTab("➕  Assign to Students", buildAssignTab());
        tabs.addTab("👥  Enrolled Students",  buildEnrolledTab());
        return tabs;
    }

    // ──────────────────────────────────────────────────────────────
    // TAB 1 – MY ASSIGNMENTS
    // ──────────────────────────────────────────────────────────────
    private JPanel buildAssignmentsTab() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBorder(new EmptyBorder(16, 16, 16, 16));
        p.setBackground(UIHelper.COLOR_BG);

        // Toolbar
        JPanel tb = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tb.setOpaque(false);
        JButton addBtn    = UIHelper.primaryButton("+ New Assignment");
        JButton editBtn   = UIHelper.outlineButton("Edit");
        JButton deleteBtn = UIHelper.dangerButton("Delete");
        JButton refreshBtn= UIHelper.outlineButton("⟳ Refresh");
        tb.add(addBtn); tb.add(editBtn); tb.add(deleteBtn); tb.add(refreshBtn);

        // Table
        String[] cols = {"ID","Title","Subject","Deadline","Description"};
        assignModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(assignModel);
        UIHelper.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(4).setPreferredWidth(260);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(220,225,235)));

        loadMyAssignments();

        addBtn.addActionListener(e -> openAssignmentForm(null));
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { UIHelper.showError(this,"Select an assignment."); return; }
            int id = (int) assignModel.getValueAt(row, 0);
            openAssignmentForm(assignDAO.getAssignmentById(id));
        });
        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { UIHelper.showError(this,"Select an assignment."); return; }
            int id = (int) assignModel.getValueAt(row, 0);
            if (UIHelper.confirm(this,"Delete this assignment?")) {
                assignDAO.deleteAssignment(id);
                loadMyAssignments();
            }
        });
        refreshBtn.addActionListener(e -> loadMyAssignments());

        p.add(tb, BorderLayout.NORTH);
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    private void loadMyAssignments() {
        assignModel.setRowCount(0);
        for (Assignment a : assignDAO.getAssignmentsByTeacher(loggedIn.getUserId())) {
            assignModel.addRow(new Object[]{
                    a.getAssignmentId(), a.getTitle(), a.getSubject(),
                    a.getDeadline().format(DT_FMT), a.getDescription()
            });
        }
    }

    private void openAssignmentForm(Assignment existing) {
        JDialog dlg = new JDialog(this,
                existing == null ? "New Assignment" : "Edit Assignment", true);
        dlg.setSize(450, 420);
        dlg.setLocationRelativeTo(this);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(24, 24, 24, 24));
        form.setBackground(UIHelper.COLOR_BG);

        JTextField titleF   = UIHelper.styledTextField();
        JTextField subjectF = UIHelper.styledTextField();
        JTextField deadlineF= UIHelper.styledTextField();
        deadlineF.setToolTipText("Format: dd-MM-yyyy HH:mm  e.g. 25-12-2025 23:59");
        JTextArea descTA = new JTextArea(4, 20);
        descTA.setFont(UIHelper.FONT_BODY);
        descTA.setLineWrap(true); descTA.setWrapStyleWord(true);
        JScrollPane descSP = new JScrollPane(descTA);

        if (existing != null) {
            titleF.setText(existing.getTitle());
            subjectF.setText(existing.getSubject());
            deadlineF.setText(existing.getDeadline().format(DT_FMT));
            descTA.setText(existing.getDescription());
        }

        addRow(form, "Title",           titleF);
        addRow(form, "Subject",         subjectF);
        addRow(form, "Deadline (dd-MM-yyyy HH:mm)", deadlineF);
        addRow(form, "Description",     descSP);
        form.add(Box.createVerticalStrut(16));

        JButton save = UIHelper.primaryButton(existing == null ? "Create" : "Update");
        save.setAlignmentX(Component.LEFT_ALIGNMENT);
        save.addActionListener(e -> {
            String title   = titleF.getText().trim();
            String subject = subjectF.getText().trim();
            String dlStr   = deadlineF.getText().trim();
            String desc    = descTA.getText().trim();

            if (title.isEmpty() || subject.isEmpty() || dlStr.isEmpty()) {
                UIHelper.showError(dlg, "Title, subject, and deadline are required."); return;
            }
            LocalDateTime deadline;
            try {
                deadline = LocalDateTime.parse(dlStr, DT_INPUT);
            } catch (DateTimeParseException ex) {
                UIHelper.showError(dlg, "Invalid date format. Use dd-MM-yyyy HH:mm"); return;
            }

            if (existing == null) {
                Assignment a = new Assignment(0, title, desc, subject, deadline, loggedIn.getUserId());
                assignDAO.addAssignment(a);
            } else {
                existing.setTitle(title); existing.setSubject(subject);
                existing.setDeadline(deadline); existing.setDescription(desc);
                assignDAO.updateAssignment(existing);
            }
            loadMyAssignments();
            dlg.dispose();
        });
        form.add(save);

        dlg.setContentPane(form);
        dlg.setVisible(true);
    }

    private void addRow(JPanel p, String label, JComponent f) {
        p.add(UIHelper.formLabel(label));
        p.add(Box.createVerticalStrut(4));
        p.add(f);
        p.add(Box.createVerticalStrut(10));
    }

    // ──────────────────────────────────────────────────────────────
    // TAB 2 – ASSIGN TO STUDENTS
    // ──────────────────────────────────────────────────────────────
    private JPanel buildAssignTab() {
        JPanel p = new JPanel(new BorderLayout(16, 0));
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
        p.setBackground(UIHelper.COLOR_BG);

        // Left – assignment list
        List<Assignment> assignments = assignDAO.getAssignmentsByTeacher(loggedIn.getUserId());
        JList<Assignment> aList = new JList<>(assignments.toArray(new Assignment[0]));
        aList.setFont(UIHelper.FONT_BODY);
        aList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane aSP = new JScrollPane(aList);
        aSP.setBorder(BorderFactory.createTitledBorder("My Assignments"));
        aSP.setPreferredSize(new Dimension(300, 0));

        // Right – student list
        List<User> students = userDAO.getUsersByRole(Role.Student);
        JList<User> sList = new JList<>(students.toArray(new User[0]));
        sList.setFont(UIHelper.FONT_BODY);
        JScrollPane sSP = new JScrollPane(sList);
        sSP.setBorder(BorderFactory.createTitledBorder("Students"));

        JButton assignBtn = UIHelper.successButton("Assign Selected →");
        assignBtn.addActionListener(e -> {
            Assignment sel = aList.getSelectedValue();
            List<User> selStudents = sList.getSelectedValuesList();
            if (sel == null || selStudents.isEmpty()) {
                UIHelper.showError(this, "Select an assignment and at least one student.");
                return;
            }
            int done = 0;
            for (User st : selStudents) {
                if (enrollDAO.enrollStudent(st.getUserId(), sel.getAssignmentId())) done++;
            }
            UIHelper.showSuccess(this, "Assigned to " + done + " student(s).");
        });

        JPanel right = new JPanel(new BorderLayout(0, 8));
        right.setOpaque(false);
        right.add(sSP, BorderLayout.CENTER);
        right.add(assignBtn, BorderLayout.SOUTH);

        p.add(aSP,   BorderLayout.WEST);
        p.add(right, BorderLayout.CENTER);
        return p;
    }

    // ──────────────────────────────────────────────────────────────
    // TAB 3 – ENROLLED STUDENTS
    // ──────────────────────────────────────────────────────────────
    private JPanel buildEnrolledTab() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBorder(new EmptyBorder(16, 16, 16, 16));
        p.setBackground(UIHelper.COLOR_BG);

        String[] cols = {"Student","Assignment","Status"};
        enrollModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(enrollModel);
        UIHelper.styleTable(table);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(220,225,235)));

        JButton refreshBtn = UIHelper.outlineButton("⟳ Refresh");
        refreshBtn.addActionListener(e -> loadEnrolled());
        loadEnrolled();

        JPanel tb = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tb.setOpaque(false); tb.add(refreshBtn);

        p.add(tb, BorderLayout.NORTH);
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    private void loadEnrolled() {
        enrollModel.setRowCount(0);
        List<Assignment> myAssignments = assignDAO.getAssignmentsByTeacher(loggedIn.getUserId());
        for (Assignment a : myAssignments) {
            for (Enrollment e : enrollDAO.getEnrollmentsByAssignment(a.getAssignmentId())) {
                enrollModel.addRow(new Object[]{
                        e.getStudentName(), a.getTitle(), e.getStatus()
                });
            }
        }
    }
}