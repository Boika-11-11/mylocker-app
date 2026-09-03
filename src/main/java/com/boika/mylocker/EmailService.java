package com.boika.mylocker;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Value("${mylocker.admin.email:}")
    private String adminEmail;

    @Value("${mylocker.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${mylocker.resend.api-key:}")
    private String resendApiKey;

    @Value("${mylocker.resend.from:HopeConnect <onboarding@resend.dev>}")
    private String fromAddress;

    private void send(String to, String subject, String html, String plain) {

        if (resendApiKey == null || resendApiKey.isBlank()) {
            log.warn("No Resend API key configured. Email to {} not sent.", to);
            return;
        }

        try {
            Resend resend = new Resend(resendApiKey);

            CreateEmailOptions options = CreateEmailOptions.builder()
                    .from(fromAddress)
                    .to(to)
                    .subject(subject)
                    .html(html)
                    .text(plain)
                    .build();

            resend.emails().send(options);

            log.info("Email sent to {} — {}", to, subject);

        } catch (Exception e) {
            log.warn("Could not send email to {}: {}", to, e.getMessage());
        }
    }

    private String wrap(String heading, String greeting, String bodyHtml,
                        String buttonLabel, String buttonUrl, String footerNote) {

        return """
            <!DOCTYPE html>
            <html>
            <body style="margin:0;padding:0;background:#F4F4F7;
                         font-family:Segoe UI,Helvetica,Arial,sans-serif;">

              <table role="presentation" width="100%%" cellpadding="0" cellspacing="0"
                     style="background:#F4F4F7;padding:32px 12px;">
                <tr><td align="center">

                  <table role="presentation" width="100%%" cellpadding="0" cellspacing="0"
                         style="max-width:520px;background:#ffffff;border-radius:12px;
                                overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.06);">

                    <tr>
                      <td style="background:#2B0B3F;padding:22px 28px;">
                        <span style="font-size:19px;font-weight:700;color:#E0518C;">Hope<span style="color:#ffffff;">Connect</span></span>
                      </td>
                    </tr>

                    <tr>
                      <td style="padding:32px 28px 8px;">
                        <h1 style="margin:0 0 18px;font-size:21px;font-weight:600;
                                   color:#1A1A1A;">%s</h1>
                        <p style="margin:0 0 14px;font-size:15px;line-height:1.6;
                                  color:#3A3A3A;">%s</p>
                        %s
                      </td>
                    </tr>

                    <tr>
                      <td align="center" style="padding:12px 28px 26px;">
                        <a href="%s"
                           style="display:inline-block;background:#A8185A;color:#ffffff;
                                  text-decoration:none;padding:14px 34px;border-radius:8px;
                                  font-size:15px;font-weight:600;">%s</a>
                      </td>
                    </tr>

                    <tr>
                      <td style="padding:0 28px 26px;">
                        <p style="margin:0 0 8px;font-size:13px;color:#6B6B6B;
                                  line-height:1.6;">
                          If the button above does not work, copy this address into your browser:
                        </p>
                        <p style="margin:0;font-size:12px;color:#A8185A;
                                  word-break:break-all;line-height:1.5;">%s</p>
                      </td>
                    </tr>

                    <tr>
                      <td style="padding:20px 28px;background:#FAFAFC;
                                 border-top:1px solid #EDEDF2;">
                        <p style="margin:0;font-size:12px;color:#8A8A96;
                                  line-height:1.6;">%s</p>
                      </td>
                    </tr>

                  </table>

                  <p style="margin:18px 0 0;font-size:11px;color:#A0A0AC;">
                    HopeConnect — private, secure file storage
                  </p>

                </td></tr>
              </table>

            </body>
            </html>
            """.formatted(heading, greeting, bodyHtml, buttonUrl, buttonLabel,
                buttonUrl, footerNote);
    }

    @Async
    public void sendPasswordReset(String toEmail, String displayName, String token) {

        String url = baseUrl + "/reset-password?token=" + token;

        String html = wrap(
                "Please reset your password",
                "Dear " + displayName + ",",
                "<p style=\"margin:0 0 14px;font-size:15px;line-height:1.6;color:#3A3A3A;\">"
                        + "A request was made to reset the password on your HopeConnect account. "
                        + "Click the button below to choose a new one.</p>",
                "Reset password",
                url,
                "This link expires in 30 minutes and can only be used once. "
                        + "If you did not request this, ignore this email — your password will not change.");

        String plain = "Dear " + displayName + ",\n\n"
                + "A request was made to reset the password on your HopeConnect account.\n\n"
                + "Open this link to choose a new password:\n" + url + "\n\n"
                + "This link expires in 30 minutes and can only be used once.\n";

        send(toEmail, "HopeConnect: reset your password", html, plain);
    }

    @Async
    public void sendInvite(String toEmail, String displayName, String token) {

        String url = baseUrl + "/reset-password?token=" + token;

        String html = wrap(
                "You have been invited",
                "Dear " + displayName + ",",
                "<p style=\"margin:0 0 14px;font-size:15px;line-height:1.6;color:#3A3A3A;\">"
                        + "An account has been created for you on HopeConnect, a private "
                        + "file and document store. Click below to set your password.</p>"
                        + "<p style=\"margin:0 0 14px;font-size:15px;line-height:1.6;color:#3A3A3A;\">"
                        + "You will sign in with <strong>" + toEmail + "</strong>.</p>",
                "Set your password",
                url,
                "This link expires in 30 minutes. If it expires, use the "
                        + "'Forgot your password' link on the sign-in page to request a new one.");

        String plain = "Dear " + displayName + ",\n\n"
                + "An account has been created for you on HopeConnect.\n\n"
                + "Set your password here:\n" + url + "\n\n"
                + "You will sign in with " + toEmail + "\n";

        send(toEmail, "You have been invited to HopeConnect", html, plain);
    }

    @Async
    public void sendRequestRejected(String toEmail, String displayName) {

        String url = baseUrl + "/contact";

        String html = wrap(
                "About your access request",
                "Dear " + displayName + ",",
                "<p style=\"margin:0 0 14px;font-size:15px;line-height:1.6;color:#3A3A3A;\">"
                        + "Thank you for your interest in HopeConnect. "
                        + "Access is not being granted at this time.</p>",
                "Get in touch",
                url,
                "If you believe this is a mistake, you are welcome to contact us using the link above.");

        String plain = "Dear " + displayName + ",\n\n"
                + "Thank you for your interest in HopeConnect.\n"
                + "Access is not being granted at this time.\n";

        send(toEmail, "HopeConnect: about your access request", html, plain);
    }

    @Async
    public void notifyAdminOfRequest(String name, String email, String reason) {

        if (adminEmail == null || adminEmail.isBlank()) {
            log.info("No admin email configured. Skipping request notification.");
            return;
        }

        String url = baseUrl + "/admin/users";

        String html = wrap(
                "New access request",
                "Someone has requested access to HopeConnect.",
                "<p style=\"margin:0 0 6px;font-size:15px;color:#3A3A3A;\"><strong>Name:</strong> "
                        + name + "</p>"
                        + "<p style=\"margin:0 0 6px;font-size:15px;color:#3A3A3A;\"><strong>Email:</strong> "
                        + email + "</p>"
                        + "<p style=\"margin:0 0 14px;font-size:15px;color:#3A3A3A;\"><strong>Reason:</strong> "
                        + (reason == null || reason.isBlank() ? "(none given)" : reason) + "</p>",
                "Review the request",
                url,
                "Approve or reject this request from the People page.");

        String plain = "New access request on HopeConnect.\n\n"
                + "Name: " + name + "\n"
                + "Email: " + email + "\n"
                + "Reason: " + (reason == null || reason.isBlank() ? "(none)" : reason) + "\n\n"
                + "Review it here: " + url + "\n";

        send(adminEmail, "HopeConnect: new access request from " + name, html, plain);
    }

    @Async
    public void sendContactMessage(String fromName, String fromEmail, String body) {

        if (adminEmail == null || adminEmail.isBlank()) {
            log.info("No admin email configured. Skipping contact message.");
            return;
        }

        String html = wrap(
                "New contact message",
                "Someone used the contact form on HopeConnect.",
                "<p style=\"margin:0 0 6px;font-size:15px;color:#3A3A3A;\"><strong>Name:</strong> "
                        + fromName + "</p>"
                        + "<p style=\"margin:0 0 14px;font-size:15px;color:#3A3A3A;\"><strong>Email:</strong> "
                        + fromEmail + "</p>"
                        + "<p style=\"margin:0 0 14px;font-size:15px;line-height:1.6;color:#3A3A3A;"
                        + "white-space:pre-wrap;\">" + body + "</p>",
                "Open HopeConnect",
                baseUrl,
                "Reply directly to " + fromEmail + " to respond.");

        String plain = "New contact message.\n\n"
                + "Name: " + fromName + "\n"
                + "Email: " + fromEmail + "\n\n"
                + body + "\n";

        send(adminEmail, "HopeConnect contact form: " + fromName, html, plain);
    }
}