package com.tracker.util;

/**
 * Change these values to your Gmail credentials.
 * For Gmail you must use an App Password, not your real password.
 * Steps to get App Password:
 *   1. Go to myaccount.google.com
 *   2. Security → 2-Step Verification → turn ON
 *   3. Security → App Passwords → generate one for "Mail"
 *   4. Paste that 16-character password below
 */
public class EmailConfig {

    public static final String SMTP_HOST     = "smtp.gmail.com";
    public static final int    SMTP_PORT     = 587;
    public static final String SENDER_EMAIL  = "rakshita1967@gmail.com";   // ← change this
    public static final String SENDER_PASS   = "nqjm rqoj cjrf huxj";   // ← App Password
    public static final String SENDER_NAME   = "Assignment Tracker";
}