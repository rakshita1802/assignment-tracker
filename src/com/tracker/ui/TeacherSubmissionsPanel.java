package com.tracker.ui;

import com.tracker.dao.AssignmentDAO;
import com.tracker.dao.GradeDAO;
import com.tracker.dao.SubmissionDAO;
import com.tracker.model.Assignment;
import com.tracker.model.Grade;
import com.tracker.model.Submission;
import com.tracker.model.User;
import com.tracker.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Teacher panel to view and grade student submissions.
 *
 * Left  → Assignment list (teacher's own)
 * Right → Table of student submissions for selected assignment
 *         with Open File / Grade actions
 */
public class TeacherSubmissionsPanel extends JPanel {

    private final User          loggedIn;
    private final AssignmentDAO assignDAO = new AssignmentDAO();
    private final SubmissionDAO subDAO    = new SubmissionDAO();
    private final GradeDAO      gradeDAO  = new GradeDAO();

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private Assignment selectedAssignment = null;
    private DefaultListModel<Assignment> assignListModel;
    private JList<Assignment>            assignList;

    private DefaultTableModel subTableModel;
    private JTable            subTable;
    private List<Submission>  currentSubs;

    private JLabel headerLbl;
    private JLabel statsLbl;

    public TeacherSubmissionsPanel(User loggedIn) {
        this.loggedIn = loggedIn;
        setLayout(new BorderLayout());
        setBackground(UIHelper.COLOR_BG);

        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                buildLeftPanel(),
                buildRightPanel()
        );
        split.setDividerLocation(260);
        split.setDividerSize(1);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);
    }

    // ──────────────────────────────────────────────────────────────
    // LEFT — Assignment list
    // ──────────────────────────────────────────────────────────────
    private JPanel buildLeftPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(new Color(248, 249, 252));
        p.setBorder(new EmptyBorder(16, 12, 16, 8));
        p.setPreferredSize(new Dimension(255, 0));

        JLabel hdr = new JLabel("My Assignments");
        hdr.setFont(UIHelper.FONT_HEADING);
        hdr.setForeground(UIHelper.COLOR_TEXT);

        assignListModel = new DefaultListModel<>();
        assignList = new JList<>(assignListModel);
        assignList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        assignList.setBackground(new Color(248, 249, 252));
        assignList.setFixedCellHeight(64);
        assignList.setCellRenderer(new AssignmentCellRenderer());

        assignList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedAssignment = assignList.getSelectedValue();
                loadSubmissions();
            }
        });

        JScrollPane sp = new JScrollPane(assignList);
        sp.setBorder(BorderFactory.createEmptyBorder());

        loadAssignments();

        p.add(hdr, BorderLayout.NORTH);
        p.add(sp,  BorderLayout.CENTER);
        return p;
    }

    private void loadAssignments() {
        assignListModel.clear();
        for (Assignment a : assignDAO.getAssignmentsByTeacher(loggedIn.getUserId())) {
            assignListModel.addElement(a);
        }
    }

    // ──────────────────────────────────────────────────────────────
    // RIGHT — Submissions table
    // ──────────────────────────────────────────────────────────────
    private JPanel buildRightPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(UIHelper.COLOR_WHITE);
        p.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Header section
        JPanel topSection = new JPanel(new BorderLayout(0, 4));
        topSection.setOpaque(false);

        headerLbl = new JLabel("Select an assignment to view submissions");
        headerLbl.setFont(UIHelper.FONT_HEADING);
        headerLbl.setForeground(UIHelper.COLOR_TEXT);

        statsLbl = new JLabel("");
        statsLbl.setFont(UIHelper.FONT_SMALL);
        statsLbl.setForeground(UIHelper.COLOR_TEXT_MUTED);

        topSection.add(headerLbl, BorderLayout.NORTH);
        topSection.add(statsLbl,  BorderLayout.SOUTH);

        // Table
        String[] cols = {"#","Student","File Name","Comment","Submitted At","Grade","Action"};
        subTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        subTable = new JTable(subTableModel);
        UIHelper.styleTable(subTable);
        subTable.getColumnModel().getColumn(0).setMaxWidth(40);
        subTable.getColumnModel().getColumn(2).setPreferredWidth(180);
        subTable.getColumnModel().getColumn(3).setPreferredWidth(200);
        subTable.getColumnModel().getColumn(5).setMaxWidth(70);
        subTable.getColumnModel().getColumn(6).setMaxWidth(80);
        subTable.setRowHeight(40);

        JScrollPane sp = new JScrollPane(subTable);
        sp.setBorder(BorderFactory.createLineBorder(new Color(220,225,235)));

        // Action buttons below table
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnPanel.setOpaque(false);

        JButton openBtn  = UIHelper.primaryButton("📂 Open File");
        JButton gradeBtn = UIHelper.successButton("✎  Grade");
        JButton refreshBtn = UIHelper.outlineButton("⟳ Refresh");

        openBtn.addActionListener(e  -> openSelectedFile());
        gradeBtn.addActionListener(e -> openGradeDialog());
        refreshBtn.addActionListener(e -> loadSubmissions());

        btnPanel.add(openBtn);
        btnPanel.add(gradeBtn);
        btnPanel.add(refreshBtn);

        p.add(topSection, BorderLayout.NORTH);
        p.add(sp,         BorderLayout.CENTER);
        p.add(btnPanel,   BorderLayout.SOUTH);
        return p;
    }

    private void loadSubmissions() {
        subTableModel.setRowCount(0);
        if (selectedAssignment == null) return;

        headerLbl.setText(selectedAssignment.getTitle());
        currentSubs = subDAO.getSubmissionsByAssignment(
                selectedAssignment.getAssignmentId());

        int total = currentSubs.size();
        statsLbl.setText(total + " submission(s) received");

        int idx = 1;
        for (Submission s : currentSubs) {
            Grade g = gradeDAO.getGrade(
                    s.getStudentId(), selectedAssignment.getAssignmentId());
            String gradeStr = g != null ?
                    g.getMarks() + "/" + g.getMaxMarks() + " (" + g.getGradeLetter() + ")" : "—";

            subTableModel.addRow(new Object[]{
                    idx++,
                    s.getStudentName(),
                    s.getFileName(),
                    s.getStudentComment() != null ? s.getStudentComment() : "",
                    s.getSubmittedAt() != null ? s.getSubmittedAt().format(FMT) : "—",
                    gradeStr,
                    "View"
            });
        }
    }

    // ── Open file ─────────────────────────────────────────────────
    private void openSelectedFile() {
        int row = subTable.getSelectedRow();
        if (row < 0 || currentSubs == null) {
            UIHelper.showError(this, "Select a submission first.");
            return;
        }
        String path = currentSubs.get(row).getFilePath();
        try {
            File f = new File(path);
            if (!f.exists()) {
                UIHelper.showError(this, "File not found:\n" + path);
                return;
            }
            Desktop.getDesktop().open(f);
        } catch (IOException ex) {
            UIHelper.showError(this, "Cannot open file: " + ex.getMessage());
        }
    }

    // ── Grade dialog ──────────────────────────────────────────────
    private void openGradeDialog() {
        int row = subTable.getSelectedRow();
        if (row < 0 || currentSubs == null) {
            UIHelper.showError(this, "Select a student submission first.");
            return;
        }
        Submission sub = currentSubs.get(row);
        Grade existing = gradeDAO.getGrade(
                sub.getStudentId(), selectedAssignment.getAssignmentId());

        JDialog dlg = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Grade — " + sub.getStudentName(),
                java.awt.Dialog.ModalityType.APPLICATION_MODAL
        );
        dlg.setSize(420, 340);
        dlg.setLocationRelativeTo(this);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(24, 24, 24, 24));
        form.setBackground(UIHelper.COLOR_BG);

        JLabel studentLbl = new JLabel("Student: " + sub.getStudentName());
        studentLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        studentLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel assignLbl = new JLabel("Assignment: " + selectedAssignment.getTitle());
        assignLbl.setFont(UIHelper.FONT_SMALL);
        assignLbl.setForeground(UIHelper.COLOR_TEXT_MUTED);
        assignLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField marksField    = UIHelper.styledTextField();
        JTextField maxMarksField = UIHelper.styledTextField();
        JTextArea  feedbackArea  = new JTextArea(4, 20);
        feedbackArea.setFont(UIHelper.FONT_BODY);
        feedbackArea.setLineWrap(true);
        feedbackArea.setWrapStyleWord(true);
        feedbackArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206,212,218),1),
                new EmptyBorder(6,10,6,10)
        ));

        if (existing != null) {
            marksField.setText(String.valueOf(existing.getMarks()));
            maxMarksField.setText(String.valueOf(existing.getMaxMarks()));
            if (existing.getFeedback() != null)
                feedbackArea.setText(existing.getFeedback());
        } else {
            maxMarksField.setText("100");
        }

        marksField.setAlignmentX(Component.LEFT_ALIGNMENT);
        maxMarksField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JScrollPane feedbackSP = new JScrollPane(feedbackArea);
        feedbackSP.setAlignmentX(Component.LEFT_ALIGNMENT);
        feedbackSP.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JButton saveBtn = UIHelper.successButton("Save Grade");
        saveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        saveBtn.addActionListener(e -> {
            try {
                int marks    = Integer.parseInt(marksField.getText().trim());
                int maxMarks = Integer.parseInt(maxMarksField.getText().trim());
                if (marks < 0 || marks > maxMarks) {
                    UIHelper.showError(dlg,
                            "Marks must be between 0 and " + maxMarks);
                    return;
                }
                Grade g = new Grade(sub.getStudentId(),
                        selectedAssignment.getAssignmentId(),
                        marks, maxMarks, feedbackArea.getText().trim());
                if (gradeDAO.addOrUpdateGrade(g)) {
                    UIHelper.showSuccess(dlg, "Grade saved!");
                    loadSubmissions();
                    dlg.dispose();
                } else {
                    UIHelper.showError(dlg, "Failed to save grade.");
                }
            } catch (NumberFormatException ex) {
                UIHelper.showError(dlg, "Enter valid numbers for marks.");
            }
        });

        form.add(studentLbl);
        form.add(Box.createVerticalStrut(4));
        form.add(assignLbl);
        form.add(Box.createVerticalStrut(16));
        form.add(UIHelper.formLabel("Marks Obtained"));
        form.add(Box.createVerticalStrut(4));
        form.add(marksField);
        form.add(Box.createVerticalStrut(10));
        form.add(UIHelper.formLabel("Maximum Marks"));
        form.add(Box.createVerticalStrut(4));
        form.add(maxMarksField);
        form.add(Box.createVerticalStrut(10));
        form.add(UIHelper.formLabel("Feedback to Student"));
        form.add(Box.createVerticalStrut(4));
        form.add(feedbackSP);
        form.add(Box.createVerticalStrut(16));
        form.add(saveBtn);

        dlg.setContentPane(form);
        dlg.setVisible(true);
    }

    // ── Assignment list renderer ──────────────────────────────────
    private class AssignmentCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            Assignment a = (Assignment) value;
            int count = subDAO.countSubmissions(a.getAssignmentId());

            JPanel card = new JPanel(new GridLayout(3, 1, 0, 2));
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0,
                            new Color(230,232,240)),
                    new EmptyBorder(8, 10, 8, 10)
            ));
            card.setBackground(isSelected ?
                    new Color(235,240,255) : new Color(248,249,252));

            JLabel t = new JLabel(a.getTitle());
            t.setFont(new Font("Segoe UI", Font.BOLD, 12));
            t.setForeground(UIHelper.COLOR_TEXT);

            JLabel sub = new JLabel(a.getSubject());
            sub.setFont(UIHelper.FONT_SMALL);
            sub.setForeground(UIHelper.COLOR_TEXT_MUTED);

            JLabel cnt = new JLabel(count + " submission" + (count != 1 ? "s" : ""));
            cnt.setFont(new Font("Segoe UI", Font.BOLD, 11));
            cnt.setForeground(count > 0 ?
                    UIHelper.COLOR_SUCCESS : UIHelper.COLOR_TEXT_MUTED);

            card.add(t);
            card.add(sub);
            card.add(cnt);
            return card;
        }
    }
}