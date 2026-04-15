package com.tracker.ui;

import com.tracker.dao.AssignmentDAO;
import com.tracker.dao.EnrollmentDAO;
import com.tracker.dao.UserDAO;
import com.tracker.model.Assignment;
import com.tracker.model.Enrollment;
import com.tracker.model.Role;
import com.tracker.model.User;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reusable analytics charts panel.
 * Pass the logged-in user to get role-specific charts.
 */
public class DashboardCharts extends JPanel {

    private final AssignmentDAO assignDAO  = new AssignmentDAO();
    private final EnrollmentDAO enrollDAO  = new EnrollmentDAO();
    private final UserDAO       userDAO    = new UserDAO();

    public DashboardCharts(User loggedIn) {
        setLayout(new GridLayout(1, 2, 16, 0));
        setOpaque(false);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        add(buildStatusPieChart(loggedIn));
        add(buildSubjectBarChart(loggedIn));
    }

    // ── PIE CHART: Completed vs Pending ──────────────────────────
    private ChartPanel buildStatusPieChart(User user) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();

        List<Enrollment> enrollments;
        if (user.getRole() == Role.Student) {
            enrollments = enrollDAO.getEnrollmentsByStudent(user.getUserId());
        } else {
            enrollments = enrollDAO.getAllEnrollments();
        }

        long completed = enrollments.stream()
                .filter(e -> "Completed".equals(e.getStatus())).count();
        long pending = enrollments.size() - completed;

        dataset.setValue("Completed (" + completed + ")", completed);
        dataset.setValue("Pending ("   + pending   + ")", pending);

        JFreeChart chart = ChartFactory.createPieChart(
                "Assignment Status", dataset, true, true, false);

        PiePlot<?> plot = (PiePlot<?>) chart.getPlot();
        plot.setSectionPaint("Completed (" + completed + ")", new Color(34, 197, 94));
        plot.setSectionPaint("Pending ("   + pending   + ")", new Color(220, 53, 69));
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));

        chart.setBackgroundPaint(Color.WHITE);
        chart.getLegend().setItemFont(new Font("Segoe UI", Font.PLAIN, 12));

        ChartPanel cp = new ChartPanel(chart);
        cp.setPreferredSize(new Dimension(380, 280));
        cp.setBackground(Color.WHITE);
        cp.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 235)));
        return cp;
    }

    // ── BAR CHART: Assignments per Subject ───────────────────────
    private ChartPanel buildSubjectBarChart(User user) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        List<Assignment> assignments;
        if (user.getRole() == Role.Teacher) {
            assignments = assignDAO.getAssignmentsByTeacher(user.getUserId());
        } else if (user.getRole() == Role.Student) {
            assignments = assignDAO.getAssignmentsByStudent(user.getUserId());
        } else {
            assignments = assignDAO.getAllAssignments();
        }

        // Count per subject
        Map<String, Integer> subjectCount = new HashMap<>();
        for (Assignment a : assignments) {
            subjectCount.merge(a.getSubject(), 1, Integer::sum);
        }
        subjectCount.forEach((subject, count) ->
                dataset.addValue(count, "Assignments", subject));

        JFreeChart chart = ChartFactory.createBarChart(
                "Assignments by Subject",
                "Subject", "Count",
                dataset,
                PlotOrientation.VERTICAL,
                false, true, false
        );

        chart.setBackgroundPaint(Color.WHITE);
        chart.getPlot().setBackgroundPaint(new Color(248, 249, 252));
        chart.getCategoryPlot().getRenderer()
                .setSeriesPaint(0, new Color(67, 97, 238));
        chart.getCategoryPlot().getDomainAxis()
                .setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        chart.getCategoryPlot().getRangeAxis()
                .setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));

        ChartPanel cp = new ChartPanel(chart);
        cp.setPreferredSize(new Dimension(380, 280));
        cp.setBackground(Color.WHITE);
        cp.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 235)));
        return cp;
    }
}