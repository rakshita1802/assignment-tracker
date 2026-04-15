package com.tracker.util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class UIHelper {

    public static final Color COLOR_BG           = new Color(245, 247, 250);
    public static final Color COLOR_PRIMARY      = new Color(67,  97, 238);
    public static final Color COLOR_PRIMARY_DARK = new Color(50,  72, 200);
    public static final Color COLOR_SUCCESS      = new Color(34, 197, 94);
    public static final Color COLOR_DANGER       = new Color(220, 53,  69);
    public static final Color COLOR_WARNING      = new Color(255, 193,  7);
    public static final Color COLOR_TEXT         = new Color(30,  30,  46);
    public static final Color COLOR_TEXT_MUTED   = new Color(108, 117, 125);
    public static final Color COLOR_WHITE        = Color.WHITE;
    public static final Color COLOR_TABLE_HEADER = new Color(52,  58,  64);
    public static final Color COLOR_TABLE_ROW_ALT= new Color(248, 249, 252);
    public static final Color COLOR_SIDEBAR      = new Color(30,  30,  46);
    public static final Color COLOR_SIDEBAR_TEXT = new Color(200, 210, 230);

    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD,  22);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD,  16);
    public static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BUTTON  = new Font("Segoe UI", Font.BOLD,  13);

    public static JButton primaryButton(String text) {
        return styledButton(text, COLOR_PRIMARY, COLOR_WHITE);
    }

    public static JButton successButton(String text) {
        return styledButton(text, COLOR_SUCCESS, COLOR_WHITE);
    }

    public static JButton dangerButton(String text) {
        return styledButton(text, COLOR_DANGER, COLOR_WHITE);
    }

    public static JButton warningButton(String text) {
        return styledButton(text, COLOR_WARNING, COLOR_TEXT);
    }

    public static JButton outlineButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BUTTON);
        btn.setForeground(COLOR_PRIMARY);
        btn.setBackground(COLOR_WHITE);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_PRIMARY, 1, true),
                new EmptyBorder(6, 16, 6, 16)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private static JButton styledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BUTTON);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 20, 8, 20));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(bg.darker()); }
            public void mouseExited (java.awt.event.MouseEvent e) { btn.setBackground(bg); }
        });
        return btn;
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(32);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(67, 97, 238, 40));
        table.setSelectionForeground(COLOR_TEXT);
        table.setBackground(COLOR_WHITE);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object value, boolean isSelected,
                    boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? COLOR_WHITE : COLOR_TABLE_ROW_ALT);
                    setForeground(COLOR_TEXT);
                }
                return this;
            }
        });
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(COLOR_TABLE_HEADER);
        header.setForeground(COLOR_WHITE);
        header.setPreferredSize(new Dimension(0, 38));
        header.setReorderingAllowed(false);
    }

    public static JTextField styledTextField() {
        JTextField tf = new JTextField();
        tf.setFont(FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218), 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        return tf;
    }

    public static JPasswordField styledPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(FONT_BODY);
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218), 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        return pf;
    }

    public static JLabel formLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(COLOR_TEXT_MUTED);
        return lbl;
    }

    public static void showSuccess(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Success",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirm(Component parent, String msg) {
        return JOptionPane.showConfirmDialog(parent, msg, "Confirm",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    private UIHelper() {}
}