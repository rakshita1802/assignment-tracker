package com.tracker.ui;

import com.tracker.dao.AssignmentDAO;
import com.tracker.dao.EnrollmentDAO;
import com.tracker.dao.SubmissionDAO;
import com.tracker.model.Assignment;
import com.tracker.model.Enrollment;
import com.tracker.model.Submission;
import com.tracker.model.User;
import com.tracker.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * GCR-style assignment submission panel for students.
 *
 * Left panel  → list of assigned assignments with status badges
 * Right panel → submission area for the selected assignment
 *               (attach file, add comment, submit / re-submit)
 */
public class SubmissionPanel extends JPanel {

    private final User          loggedIn;
    private final AssignmentDAO assignDAO  = new AssignmentDAO();
    private final EnrollmentDAO enrollDAO  = new EnrollmentDAO();
    private final SubmissionDAO subDAO     = new SubmissionDAO();

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    // Submission storage folder — create this folder in your project root
    private static final String UPLOAD_DIR = "submissions/";

    // ── State ─────────────────────────────────────────────────────
    private Assignment  selectedAssignment = null;
    private File        selectedFile       = null;
    private Submission  existingSubmission = null;

    // ── Right-panel components (updated per selection) ────────────
    private JPanel      rightPanel;
    private JLabel      assignTitleLbl;
    private JLabel      assignSubjectLbl;
    private JLabel      assignDeadlineLbl;
    private JLabel      assignDescLbl;
    private JLabel      fileNameLbl;
    private JTextArea   commentArea;
    private JButton     attachBtn;
    private JButton     submitBtn;
    private JButton     removeFileBtn;
    private JPanel      existingSubPanel;
    private JLabel      existingFileLbl;
    private JLabel      submittedAtLbl;
    private JList<Assignment> assignList;
    private DefaultListModel<Assignment> listModel;

    public SubmissionPanel(User loggedIn) {
        this.loggedIn = loggedIn;
        setLayout(new BorderLayout(0, 0));
        setBackground(UIHelper.COLOR_BG);

        // Ensure upload directory exists
        new File(UPLOAD_DIR).mkdirs();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildLeftPanel(), buildRightPanel());
        split.setDividerLocation(300);
        split.setDividerSize(1);
        split.setBorder(null);

        add(split, BorderLayout.CENTER);
    }

    // ──────────────────────────────────────────────────────────────
    // LEFT PANEL — Assignment list
    // ──────────────────────────────────────────────────────────────
    private JPanel buildLeftPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(new Color(248, 249, 252));
        p.setBorder(new EmptyBorder(16, 12, 16, 8));
        p.setPreferredSize(new Dimension(290, 0));

        JLabel header = new JLabel("Your Assignments");
        header.setFont(UIHelper.FONT_HEADING);
        header.setForeground(UIHelper.COLOR_TEXT);
        header.setBorder(new EmptyBorder(0, 4, 8, 0));

        listModel = new DefaultListModel<>();
        assignList = new JList<>(listModel);
        assignList.setCellRenderer(new AssignmentListRenderer());
        assignList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        assignList.setBackground(new Color(248, 249, 252));
        assignList.setFixedCellHeight(72);

        assignList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedAssignment = assignList.getSelectedValue();
                selectedFile = null;
                refreshRightPanel();
            }
        });

        JScrollPane sp = new JScrollPane(assignList);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setBackground(new Color(248, 249, 252));

        loadAssignments();

        p.add(header, BorderLayout.NORTH);
        p.add(sp,     BorderLayout.CENTER);
        return p;
    }

    private void loadAssignments() {
        listModel.clear();
        List<Assignment> assignments =
                assignDAO.getAssignmentsByStudent(loggedIn.getUserId());
        for (Assignment a : assignments) listModel.addElement(a);
    }

    // ──────────────────────────────────────────────────────────────
    // RIGHT PANEL — Submission area
    // ──────────────────────────────────────────────────────────────
    private JPanel buildRightPanel() {
        rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(UIHelper.COLOR_WHITE);
        rightPanel.setBorder(new EmptyBorder(24, 24, 24, 24));

        showEmptyState();
        return rightPanel;
    }

    private void showEmptyState() {
        rightPanel.removeAll();
        JLabel lbl = new JLabel("← Select an assignment to submit your work",
                SwingConstants.CENTER);
        lbl.setFont(UIHelper.FONT_BODY);
        lbl.setForeground(UIHelper.COLOR_TEXT_MUTED);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        rightPanel.add(Box.createVerticalGlue());
        rightPanel.add(lbl);
        rightPanel.add(Box.createVerticalGlue());
        rightPanel.revalidate();
        rightPanel.repaint();
    }

    private void refreshRightPanel() {
        if (selectedAssignment == null) { showEmptyState(); return; }

        existingSubmission = subDAO.getSubmission(
                loggedIn.getUserId(), selectedAssignment.getAssignmentId());

        rightPanel.removeAll();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        // ── Assignment info card ──────────────────────────────────
        JPanel infoCard = new JPanel(new GridLayout(0, 1, 0, 4));
        infoCard.setBackground(new Color(240, 244, 255));
        infoCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 200, 255), 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));
        infoCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        infoCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        assignTitleLbl   = boldLabel(selectedAssignment.getTitle(), 16);
        assignSubjectLbl = mutedLabel("Subject: " + selectedAssignment.getSubject());
        assignDeadlineLbl= mutedLabel("Deadline: " + selectedAssignment.getDeadline().format(FMT));
        assignDescLbl    = mutedLabel(selectedAssignment.getDescription() != null ?
                selectedAssignment.getDescription() : "No description provided.");

        infoCard.add(assignTitleLbl);
        infoCard.add(assignSubjectLbl);
        infoCard.add(assignDeadlineLbl);
        infoCard.add(assignDescLbl);
        rightPanel.add(infoCard);
        rightPanel.add(Box.createVerticalStrut(20));

        // ── Existing submission banner ────────────────────────────
        if (existingSubmission != null) {
            JPanel banner = new JPanel(new GridLayout(0, 1, 0, 4));
            banner.setBackground(new Color(236, 253, 245));
            banner.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(52, 211, 153), 1, true),
                    new EmptyBorder(10, 14, 10, 14)
            ));
            banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
            banner.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel tick = new JLabel("✔  Work Submitted");
            tick.setFont(new Font("Segoe UI", Font.BOLD, 13));
            tick.setForeground(new Color(6, 120, 70));

            existingFileLbl = new JLabel("📎  " + existingSubmission.getFileName());
            existingFileLbl.setFont(UIHelper.FONT_SMALL);
            existingFileLbl.setForeground(UIHelper.COLOR_TEXT_MUTED);

            submittedAtLbl = new JLabel("Submitted: " +
                    (existingSubmission.getSubmittedAt() != null ?
                            existingSubmission.getSubmittedAt().format(FMT) : "—"));
            submittedAtLbl.setFont(UIHelper.FONT_SMALL);
            submittedAtLbl.setForeground(UIHelper.COLOR_TEXT_MUTED);

            // Open submitted file button
            JButton openBtn = new JButton("Open File");
            openBtn.setFont(UIHelper.FONT_SMALL);
            openBtn.setForeground(UIHelper.COLOR_PRIMARY);
            openBtn.setBorderPainted(false);
            openBtn.setContentAreaFilled(false);
            openBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            openBtn.addActionListener(e -> openFile(existingSubmission.getFilePath()));

            banner.add(tick);
            banner.add(existingFileLbl);
            banner.add(submittedAtLbl);
            banner.add(openBtn);
            rightPanel.add(banner);
            rightPanel.add(Box.createVerticalStrut(16));
        }

        // ── Divider label ─────────────────────────────────────────
        JLabel uploadLabel = boldLabel(
                existingSubmission == null ? "Upload Your Work" : "Re-submit Work", 14);
        uploadLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(uploadLabel);
        rightPanel.add(Box.createVerticalStrut(10));

        // ── File attach area ──────────────────────────────────────
        JPanel fileArea = new JPanel(new BorderLayout(10, 0));
        fileArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        fileArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        fileArea.setOpaque(false);

        attachBtn = UIHelper.outlineButton("📎  Choose File");
        attachBtn.addActionListener(e -> chooseFile());

        fileNameLbl = new JLabel(selectedFile != null ?
                selectedFile.getName() : "No file chosen");
        fileNameLbl.setFont(UIHelper.FONT_SMALL);
        fileNameLbl.setForeground(UIHelper.COLOR_TEXT_MUTED);

        removeFileBtn = new JButton("✕");
        removeFileBtn.setFont(UIHelper.FONT_SMALL);
        removeFileBtn.setForeground(UIHelper.COLOR_DANGER);
        removeFileBtn.setBorderPainted(false);
        removeFileBtn.setContentAreaFilled(false);
        removeFileBtn.setVisible(selectedFile != null);
        removeFileBtn.addActionListener(e -> {
            selectedFile = null;
            fileNameLbl.setText("No file chosen");
            fileNameLbl.setForeground(UIHelper.COLOR_TEXT_MUTED);
            removeFileBtn.setVisible(false);
        });

        fileArea.add(attachBtn,   BorderLayout.WEST);
        fileArea.add(fileNameLbl, BorderLayout.CENTER);
        fileArea.add(removeFileBtn, BorderLayout.EAST);
        rightPanel.add(fileArea);
        rightPanel.add(Box.createVerticalStrut(14));

        // ── Accepted file types note ──────────────────────────────
        JLabel typeNote = mutedLabel("Accepted: PDF, DOC, DOCX, TXT, ZIP, PNG, JPG");
        typeNote.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(typeNote);
        rightPanel.add(Box.createVerticalStrut(16));

        // ── Comment / note to teacher ─────────────────────────────
        JLabel commentLabel = boldLabel("Add a Comment (optional)", 13);
        commentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(commentLabel);
        rightPanel.add(Box.createVerticalStrut(6));

        commentArea = new JTextArea(4, 30);
        commentArea.setFont(UIHelper.FONT_BODY);
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        commentArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
                new EmptyBorder(8, 10, 8, 10)
        ));
        if (existingSubmission != null && existingSubmission.getStudentComment() != null) {
            commentArea.setText(existingSubmission.getStudentComment());
        }
        JScrollPane commentSP = new JScrollPane(commentArea);
        commentSP.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        commentSP.setAlignmentX(Component.LEFT_ALIGNMENT);
        commentSP.setBorder(BorderFactory.createEmptyBorder());
        rightPanel.add(commentSP);
        rightPanel.add(Box.createVerticalStrut(20));

        // ── Submit button ─────────────────────────────────────────
        submitBtn = UIHelper.primaryButton(
                existingSubmission == null ? "Submit Work" : "Update Submission");
        submitBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        submitBtn.addActionListener(e -> handleSubmit());
        rightPanel.add(submitBtn);

        rightPanel.revalidate();
        rightPanel.repaint();
    }

    // ── File chooser ──────────────────────────────────────────────
    private void chooseFile() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Choose your assignment file");
        fc.setFileFilter(new FileNameExtensionFilter(
                "Allowed files (PDF, DOC, DOCX, TXT, ZIP, PNG, JPG)",
                "pdf","doc","docx","txt","zip","png","jpg","jpeg"));
        fc.setMultiSelectionEnabled(false);

        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedFile = fc.getSelectedFile();
            fileNameLbl.setText(selectedFile.getName());
            fileNameLbl.setForeground(UIHelper.COLOR_TEXT);
            removeFileBtn.setVisible(true);
        }
    }

    // ── Submit handler ────────────────────────────────────────────
    private void handleSubmit() {
        // Must have a file unless re-submitting (keep old file)
        if (selectedFile == null && existingSubmission == null) {
            UIHelper.showError(this,
                    "Please choose a file to submit.");
            return;
        }

        String comment = commentArea.getText().trim();

        try {
            Submission sub = new Submission();
            sub.setStudentId(loggedIn.getUserId());
            sub.setAssignmentId(selectedAssignment.getAssignmentId());
            sub.setStudentComment(comment);

            if (selectedFile != null) {
                // Copy file into uploads directory with a unique name
                String uniqueName = loggedIn.getUserId() + "_" +
                        selectedAssignment.getAssignmentId() + "_" +
                        selectedFile.getName();
                Path dest = Paths.get(UPLOAD_DIR, uniqueName);
                Files.copy(selectedFile.toPath(), dest,
                        StandardCopyOption.REPLACE_EXISTING);
                sub.setFileName(selectedFile.getName());  // original name for display
                sub.setFilePath(dest.toAbsolutePath().toString());
            } else {
                // Re-submit keeping the same file, just updating comment
                sub.setFileName(existingSubmission.getFileName());
                sub.setFilePath(existingSubmission.getFilePath());
            }

            if (subDAO.saveSubmission(sub)) {
                // Mark enrollment as completed
                new EnrollmentDAO().markCompleted(
                        loggedIn.getUserId(),
                        selectedAssignment.getAssignmentId());

                UIHelper.showSuccess(this,
                        existingSubmission == null ?
                                "Work submitted successfully!" :
                                "Submission updated successfully!");

                selectedFile = null;
                loadAssignments();
                refreshRightPanel();
            } else {
                UIHelper.showError(this, "Submission failed. Try again.");
            }

        } catch (IOException e) {
            UIHelper.showError(this, "File copy error: " + e.getMessage());
        }
    }

    // ── Open file in OS default app ───────────────────────────────
    private void openFile(String filePath) {
        try {
            File f = new File(filePath);
            if (!f.exists()) {
                UIHelper.showError(this, "File not found: " + filePath);
                return;
            }
            Desktop.getDesktop().open(f);
        } catch (IOException e) {
            UIHelper.showError(this, "Cannot open file: " + e.getMessage());
        }
    }

    // ── Helper label factories ────────────────────────────────────
    private JLabel boldLabel(String text, int size) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, size));
        l.setForeground(UIHelper.COLOR_TEXT);
        return l;
    }

    private JLabel mutedLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UIHelper.FONT_SMALL);
        l.setForeground(UIHelper.COLOR_TEXT_MUTED);
        return l;
    }

    // ──────────────────────────────────────────────────────────────
    // Custom list cell renderer — GCR card style
    // ──────────────────────────────────────────────────────────────
    private class AssignmentListRenderer
            extends DefaultListCellRenderer {

        private final SubmissionDAO sDao = new SubmissionDAO();
        private final EnrollmentDAO eDao = new EnrollmentDAO();

        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {

            Assignment a = (Assignment) value;

            JPanel card = new JPanel(new BorderLayout(0, 3));
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0,
                            new Color(230, 232, 240)),
                    new EmptyBorder(10, 10, 10, 10)
            ));
            card.setBackground(isSelected ?
                    new Color(235, 240, 255) : new Color(248, 249, 252));

            // Title
            JLabel titleLbl = new JLabel(a.getTitle());
            titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
            titleLbl.setForeground(UIHelper.COLOR_TEXT);

            // Subject + deadline
            JLabel subLbl = new JLabel(a.getSubject() + "  •  Due: " +
                    a.getDeadline().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
            subLbl.setFont(UIHelper.FONT_SMALL);
            subLbl.setForeground(UIHelper.COLOR_TEXT_MUTED);

            // Status badge
            Submission sub = sDao.getSubmission(loggedIn.getUserId(), a.getAssignmentId());
            JLabel badge = new JLabel(sub != null ? "✔ Submitted" : "Pending");
            badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
            badge.setOpaque(true);
            badge.setBorder(new EmptyBorder(2, 8, 2, 8));
            if (sub != null) {
                badge.setBackground(new Color(209, 250, 229));
                badge.setForeground(new Color(6, 95, 70));
            } else {
                badge.setBackground(new Color(254, 243, 199));
                badge.setForeground(new Color(146, 64, 14));
            }

            JPanel top = new JPanel(new BorderLayout());
            top.setOpaque(false);
            top.add(titleLbl, BorderLayout.WEST);
            top.add(badge,    BorderLayout.EAST);

            card.add(top,    BorderLayout.NORTH);
            card.add(subLbl, BorderLayout.SOUTH);
            return card;
        }
    }
}