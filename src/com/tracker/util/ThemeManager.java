package com.tracker.util;

import javax.swing.*;
import java.awt.*;

/**
 * Manages light / dark theme switching for the entire application.
 * Call ThemeManager.apply(isDark) to switch themes at runtime.
 */
public class ThemeManager {

    private static boolean darkMode = false;

    // ── Light theme colors ────────────────────────────────────────
    public static final Color LIGHT_BG           = new Color(245, 247, 250);
    public static final Color LIGHT_SURFACE      = Color.WHITE;
    public static final Color LIGHT_TEXT         = new Color(30,  30,  46);
    public static final Color LIGHT_TEXT_MUTED   = new Color(108, 117, 125);
    public static final Color LIGHT_BORDER       = new Color(220, 225, 235);
    public static final Color LIGHT_SIDEBAR      = new Color(30,  30,  46);

    // ── Dark theme colors ─────────────────────────────────────────
    public static final Color DARK_BG            = new Color(18,  18,  27);
    public static final Color DARK_SURFACE       = new Color(30,  30,  46);
    public static final Color DARK_TEXT          = new Color(220, 225, 240);
    public static final Color DARK_TEXT_MUTED    = new Color(140, 150, 170);
    public static final Color DARK_BORDER        = new Color(50,  55,  75);
    public static final Color DARK_SIDEBAR       = new Color(12,  12,  20);

    public static boolean isDark() { return darkMode; }

    public static Color bg()        { return darkMode ? DARK_BG       : LIGHT_BG; }
    public static Color surface()   { return darkMode ? DARK_SURFACE  : LIGHT_SURFACE; }
    public static Color text()      { return darkMode ? DARK_TEXT     : LIGHT_TEXT; }
    public static Color textMuted() { return darkMode ? DARK_TEXT_MUTED: LIGHT_TEXT_MUTED; }
    public static Color border()    { return darkMode ? DARK_BORDER   : LIGHT_BORDER; }
    public static Color sidebar()   { return darkMode ? DARK_SIDEBAR  : LIGHT_SIDEBAR; }

    /**
     * Applies the selected theme to all open Swing windows.
     * Call this after toggling darkMode.
     */
    public static void apply(boolean dark) {
        darkMode = dark;

        // Update UIManager defaults so new components pick up the theme
        UIManager.put("Panel.background",          bg());
        UIManager.put("OptionPane.background",     bg());
        UIManager.put("TextField.background",      surface());
        UIManager.put("TextField.foreground",      text());
        UIManager.put("TextArea.background",       surface());
        UIManager.put("TextArea.foreground",       text());
        UIManager.put("Table.background",          surface());
        UIManager.put("Table.foreground",          text());
        UIManager.put("TableHeader.background",    sidebar());
        UIManager.put("TableHeader.foreground",    DARK_TEXT);
        UIManager.put("ScrollPane.background",     bg());
        UIManager.put("TabbedPane.background",     bg());
        UIManager.put("TabbedPane.foreground",     text());
        UIManager.put("Label.foreground",          text());
        UIManager.put("ComboBox.background",       surface());
        UIManager.put("ComboBox.foreground",       text());
        UIManager.put("Button.background",         surface());
        UIManager.put("Button.foreground",         text());

        // Repaint all open windows
        for (Window w : Window.getWindows()) {
            SwingUtilities.updateComponentTreeUI(w);
            w.repaint();
        }
    }

    /**
     * Adds a dark mode toggle button to any panel.
     * Automatically saves preference via UserDAO.
     */
    public static JButton createToggleButton(int userId) {
        JButton btn = new JButton(darkMode ? "☀ Light Mode" : "🌙 Dark Mode");
        btn.setFont(UIHelper.FONT_BUTTON);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setBackground(darkMode ? new Color(60,65,85) : new Color(240,242,248));
        btn.setForeground(darkMode ? Color.WHITE : UIHelper.COLOR_TEXT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            boolean newDark = !darkMode;
            apply(newDark);
            btn.setText(newDark ? "☀ Light Mode" : "🌙 Dark Mode");
            btn.setBackground(newDark ? new Color(60,65,85) : new Color(240,242,248));
            btn.setForeground(newDark ? Color.WHITE : UIHelper.COLOR_TEXT);
            // Save preference
            new com.tracker.dao.UserDAO().updateTheme(userId, newDark ? "dark" : "light");
        });

        return btn;
    }

    private ThemeManager() {}
}