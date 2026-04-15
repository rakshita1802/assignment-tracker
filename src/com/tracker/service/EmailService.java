package com.tracker.service;

import com.tracker.util.EmailConfig;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

/**
 * Sends HTML emails using Gmail SMTP.
 * Uses JavaMail (jakarta.mail) library.
 */
public class EmailService {

    private final Session session;

    public EmailService() {
        Properties props = new Properties();
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host",            EmailConfig.SMTP_HOST);
        props.put("mail.smtp.port",            EmailConfig.SMTP_PORT);
        props.put("mail.smtp.ssl.trust",       EmailConfig.SMTP_HOST);

        session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                        EmailConfig.SENDER_EMAIL,
                        EmailConfig.SENDER_PASS
                );
            }
        });
    }

    /**
     * Sends a plain text email.
     *
     * @param toEmail   recipient email address
     * @param toName    recipient name (for greeting)
     * @param subject   email subject
     * @param body      plain text body
     * @return true if sent successfully
     */
    public boolean sendEmail(String toEmail, String toName,
                             String subject, String body) {
        try {
            Message message = new MimeMessage(session);

            message.setFrom(new InternetAddress(
                    EmailConfig.SENDER_EMAIL, EmailConfig.SENDER_NAME));

            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(toEmail));

            message.setSubject(subject);

            // Build HTML email body
            String htmlBody = buildHtmlEmail(toName, body);
            message.setContent(htmlBody, "text/html; charset=utf-8");

            Transport.send(message);

            System.out.println("[EmailService] Email sent to " + toEmail);
            return true;

        } catch (Exception e) {
            System.err.println("[EmailService] Failed to send to "
                    + toEmail + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Wraps the message in a clean HTML template.
     */
    private String buildHtmlEmail(String name, String body) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="UTF-8">
              <style>
                body { font-family: 'Segoe UI', Arial, sans-serif;
                       background: #f5f7fa; margin: 0; padding: 0; }
                .container { max-width: 580px; margin: 40px auto;
                             background: #ffffff; border-radius: 10px;
                             overflow: hidden;
                             box-shadow: 0 2px 12px rgba(0,0,0,0.08); }
                .header { background: #4361ee; padding: 28px 32px; }
                .header h1 { color: #ffffff; margin: 0;
                              font-size: 22px; font-weight: 600; }
                .header p  { color: #c8d3ff; margin: 4px 0 0;
                              font-size: 13px; }
                .body   { padding: 32px; color: #1e1e2e; font-size: 15px;
                          line-height: 1.7; }
                .alert  { background: #fff4e5; border-left: 4px solid #f59e0b;
                          padding: 14px 18px; border-radius: 6px;
                          margin: 20px 0; font-size: 14px; color: #92400e; }
                .footer { background: #f1f3f9; padding: 18px 32px;
                          font-size: 12px; color: #6c757d;
                          text-align: center; }
              </style>
            </head>
            <body>
              <div class="container">
                <div class="header">
                  <h1>📚 Assignment Tracker</h1>
                  <p>Deadline Reminder Notification</p>
                </div>
                <div class="body">
                  <p>Dear <strong>%s</strong>,</p>
                  <div class="alert">%s</div>
                  <p>Please make sure to submit your assignment on time.
                     Log in to the system to view full details.</p>
                  <p>Good luck!<br>
                     <strong>Assignment Tracker Team</strong></p>
                </div>
                <div class="footer">
                  This is an automated notification. Please do not reply.
                </div>
              </div>
            </body>
            </html>
            """.formatted(name, body);
    }
}