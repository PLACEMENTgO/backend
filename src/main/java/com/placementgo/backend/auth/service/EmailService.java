package com.placementgo.backend.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        if (mailSender == null) {
            log.warn("JavaMailSender not configured — skipping email to {}", toEmail);
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "PlacementGO");
            helper.setTo(toEmail);
            helper.setSubject("Reset your PlacementGO password");

            String html = """
                <div style="font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;max-width:480px;margin:0 auto;background:#fff;border:1px solid #e2e8f0;border-radius:12px;overflow:hidden;">
                  <div style="background:#2563EB;padding:32px;text-align:center;">
                    <span style="color:white;font-size:22px;font-weight:700;">📄 PlacementGO</span>
                  </div>
                  <div style="padding:40px 32px;">
                    <h2 style="margin:0 0 8px;font-size:22px;color:#0f172a;">Reset your password</h2>
                    <p style="color:#64748b;margin:0 0 24px;line-height:1.6;">
                      We received a request to reset the password for your PlacementGO account.
                      Click the button below to choose a new password.
                    </p>
                    <a href="%s"
                       style="display:block;background:#2563EB;color:white;text-decoration:none;text-align:center;padding:14px 24px;border-radius:8px;font-weight:600;font-size:15px;margin-bottom:24px;">
                      Reset Password →
                    </a>
                    <p style="color:#94a3b8;font-size:13px;margin:0 0 8px;">
                      This link expires in <strong>15 minutes</strong>.
                    </p>
                    <p style="color:#94a3b8;font-size:13px;margin:0;">
                      If you didn't request a password reset, you can safely ignore this email.
                    </p>
                  </div>
                  <div style="background:#f8fafc;padding:20px 32px;border-top:1px solid #e2e8f0;text-align:center;">
                    <p style="color:#94a3b8;font-size:12px;margin:0;">
                      PlacementGO — AI Career Platform ·
                      <a href="https://placementgo.in" style="color:#2563EB;text-decoration:none;">placementgo.in</a>
                    </p>
                  </div>
                </div>
                """.formatted(resetLink);

            helper.setText(html, true);
            mailSender.send(message);
            log.info("Password reset email sent to {}", toEmail);

        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }
}
