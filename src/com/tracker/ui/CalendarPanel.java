package com.tracker.ui;

import com.tracker.dao.AssignmentDAO;
import com.tracker.model.Assignment;
import com.tracker.model.User;
import com.tracker.model.Role;
import com.tracker.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * Monthly calendar panel showing assignment deadlines as colored dots.
 */
public class CalendarPanel extends JPanel {

    private final AssignmentDAO assignDAO = new AssignmentDAO();
    private final User          loggedIn;

    private YearMonth currentMonth = YearMonth.now();

    // Map of day → list of assignment titles due that day
    private Map<Integer, List<String>> deadlineMap = new HashMap<>();

    private JLabel monthLabel;
    private JPanel gridPanel;

    private static final String[] DAY_NAMES =
            {"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};

    public CalendarPanel(User user) {
        this.loggedIn = user;
        setLayout(new BorderLayout(0, 8));
        setBackground(UIHelper.COLOR_WHITE);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        add(buildNavBar(),  BorderLayout.NORTH);
        gridPanel = new JPanel();
        add(gridPanel, BorderLayout.CENTER);

        loadAndRender();
    }

    // ── Navigation bar ────────────────────────────────────────────
    private JPanel buildNavBar() {
        JPanel nav = new JPanel(new BorderLayout());
        nav.setOpaque(false);

        JButton prev = UIHelper.outlineButton("◀  Prev");
        JButton next = UIHelper.outlineButton("Next  ▶");
        monthLabel   = new JLabel("", SwingConstants.CENTER);
        monthLabel.setFont(UIHelper.FONT_HEADING);
        monthLabel.setForeground(UIHelper.COLOR_TEXT);

        prev.addActionListener(e -> {
            currentMonth = currentMonth.minusMonths(1);
            loadAndRender();
        });
        next.addActionListener(e -> {
            currentMonth = currentMonth.plusMonths(1);
            loadAndRender();
        });

        nav.add(prev,       BorderLayout.WEST);
        nav.add(monthLabel, BorderLayout.CENTER);
        nav.add(next,       BorderLayout.EAST);
        return nav;
    }

    // ── Load deadlines + render grid ─────────────────────────────
    private void loadAndRender() {
        deadlineMap.clear();

        // Fetch assignments for this user
        List<Assignment> assignments;
        if (loggedIn.getRole() == Role.Student) {
            assignments = assignDAO.getAssignmentsByStudent(loggedIn.getUserId());
        } else if (loggedIn.getRole() == Role.Teacher) {
            assignments = assignDAO.getAssignmentsByTeacher(loggedIn.getUserId());
        } else {
            assignments = assignDAO.getAllAssignments();
        }

        // Group by day of month (only for current displayed month)
        for (Assignment a : assignments) {
            LocalDateTime dl = a.getDeadline();
            if (dl.getYear() == currentMonth.getYear() &&
                    dl.getMonthValue() == currentMonth.getMonthValue()) {
                int day = dl.getDayOfMonth();
                deadlineMap.computeIfAbsent(day, k -> new ArrayList<>()).add(a.getTitle());
            }
        }

        monthLabel.setText(currentMonth.getMonth().name() + "  " + currentMonth.getYear());
        renderGrid();
    }

    private void renderGrid() {
        gridPanel.removeAll();
        gridPanel.setLayout(new GridLayout(0, 7, 4, 4));
        gridPanel.setOpaque(false);

        // Day name headers
        for (String d : DAY_NAMES) {
            JLabel lbl = new JLabel(d, SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lbl.setForeground(UIHelper.COLOR_TEXT_MUTED);
            gridPanel.add(lbl);
        }

        // Blank cells before first day
        int firstDayOfWeek = currentMonth.atDay(1).getDayOfWeek().getValue() % 7;
        for (int i = 0; i < firstDayOfWeek; i++) {
            gridPanel.add(new JLabel(""));
        }

        // Day cells
        int daysInMonth = currentMonth.lengthOfMonth();
        int today = LocalDate.now().getDayOfMonth();
        boolean isCurrentMonth = currentMonth.equals(YearMonth.now());

        for (int day = 1; day <= daysInMonth; day++) {
            gridPanel.add(buildDayCell(day, isCurrentMonth && day == today));
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JPanel buildDayCell(int day, boolean isToday) {
        JPanel cell = new JPanel(new BorderLayout(0, 2));
        cell.setPreferredSize(new Dimension(60, 60));
        cell.setBorder(new EmptyBorder(4, 4, 4, 4));

        boolean hasDeadline = deadlineMap.containsKey(day);

        if (isToday) {
            cell.setBackground(UIHelper.COLOR_PRIMARY);
        } else if (hasDeadline) {
            cell.setBackground(new Color(255, 243, 224));
        } else {
            cell.setBackground(UIHelper.COLOR_WHITE);
        }

        cell.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 235), 1),
                new EmptyBorder(4, 4, 4, 4)
        ));

        // Day number
        JLabel dayLbl = new JLabel(String.valueOf(day), SwingConstants.CENTER);
        dayLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        dayLbl.setForeground(isToday ? Color.WHITE :
                hasDeadline ? new Color(180, 80, 0) : UIHelper.COLOR_TEXT);
        cell.add(dayLbl, BorderLayout.NORTH);

        // Deadline dot + tooltip
        if (hasDeadline) {
            List<String> titles = deadlineMap.get(day);
            JLabel dot = new JLabel("●", SwingConstants.CENTER);
            dot.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            dot.setForeground(UIHelper.COLOR_DANGER);
            String tooltip = "<html>" + String.join("<br>", titles) + "</html>";
            cell.setToolTipText(tooltip);
            dot.setToolTipText(tooltip);
            cell.add(dot, BorderLayout.CENTER);

            // Count badge if multiple
            if (titles.size() > 1) {
                JLabel count = new JLabel(titles.size() + " due", SwingConstants.CENTER);
                count.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                count.setForeground(new Color(180, 80, 0));
                cell.add(count, BorderLayout.SOUTH);
            }
        }

        return cell;
    }
}