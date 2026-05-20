package com.university.project.hotelmanagement.services;

import com.university.project.hotelmanagement.entity.EmailLog;
import com.university.project.hotelmanagement.repository.EmailLogRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailLogRepository logRepository;

    private static final String FROM_EMAIL = "no-reply@hotelapp.com";
    private static final String APP_NAME = "Hotel Management System";


    @Async
    public void sendOtpEmail(String toEmail, String userName, String otp) {
        sendOtpWithRetry(toEmail, userName, otp, 3);
    }

    public void sendOtpWithRetry(String toEmail, String userName, String otp, int maxAttempts) {

        String subject = "OTP Verification - " + APP_NAME;

        for (int i = 1; i <= maxAttempts; i++) {
            try {

                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setTo(toEmail);
                helper.setFrom(FROM_EMAIL);
                helper.setSubject(subject);

                helper.setText(buildRegistrationOtpTemplate(userName, otp), true);

                mailSender.send(message);

                log(toEmail, subject, "SUCCESS", null);
                return;

            } catch (Exception e) {
                if (i == maxAttempts) {
                    log(toEmail, subject, "FAILED", e.getMessage());
                    throw new RuntimeException("OTP email failed");
                }
            }
        }
    }

    private String buildRegistrationOtpTemplate(String userName, String otp) {
        String primaryColor = "#1a1a1a";
        String accentColor = "#c5a059";
        String secondaryBg = "#f9f9f9";

        return "<div style='font-family: \"Georgia\", serif; background-color: #f0f2f5; padding: 40px 10px;'>" +
                "<div style='max-width: 500px; margin: 0 auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.1); border: 1px solid #e1e4e8;'>" +

                "<div style='padding: 40px; color: #444; line-height: 1.6; text-align: center;'>" +

                "<h2 style='color: " + primaryColor + "; margin-top: 0; font-size: 26px; font-weight: bold;'> "+APP_NAME+"</h2>" +
                "<div style='width: 50px; height: 3px; background-color: " + accentColor + "; margin: 15px auto;'></div>" + // Decorative Divider

                "<p style='font-size: 16px; margin-top: 25px;'>Dear <b>" + userName + "</b>,</p>" +
                "<p>Thank you for choosing us. To complete your registration and verify your account, please use the secure code below:</p>" +

                "<div style='margin: 35px 0; background-color: " + secondaryBg + "; padding: 30px; border-radius: 8px; border: 1px dashed " + accentColor + ";'>" +
                "<div style='font-size: 30px; font-weight: 600; letter-spacing: 10px; color: " + primaryColor + ";'>" +
                otp +
                "</div>" +
                "<p style='color: " + accentColor + "; font-size: 12px; margin-top: 10px; text-transform: uppercase; font-weight: bold; letter-spacing: 1px;'>Verification Code</p>" +
                "</div>" +

                "<p style='font-size: 14px; color: #666;'>" +
                "This code is valid for 10 minutes. If you did not sign up for an account, please ignore this email." +
                "</p>" +
                "</div>" +

                "<div style='background-color: " + secondaryBg + "; padding: 25px; text-align: center; border-top: 1px solid #eee; font-size: 12px; color: #888;'>" +
                "<p style='margin: 5px 0;'>&copy; 2026 " + APP_NAME + " | Azamgarh, Uttar Pradesh</p>" +
                "<p style='margin: 10px 0 0 0; color: #333;'>Developed by <b>Manish Verma</b></p>" +
                "</div>" +

                "</div>" +
                "</div>";
    }



    @Async
    public void sendForgotPasswordEmail(String toEmail, String userName, String otp) {
        sendForgotPasswordWithRetry(toEmail, userName, otp, 3);
    }
    public void sendForgotPasswordWithRetry(String toEmail, String userName, String otp, int maxAttempts) {

        String subject = "Password Reset OTP - " + APP_NAME;

        for (int i = 1; i <= maxAttempts; i++) {
            try {

                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setTo(toEmail);
                helper.setFrom(FROM_EMAIL);
                helper.setSubject(subject);

                helper.setText(buildForgotPasswordTemplate(userName, otp), true);

                mailSender.send(message);

                log(toEmail, subject, "SUCCESS", null);
                return;

            } catch (Exception e) {
                if (i == maxAttempts) {
                    log(toEmail, subject, "FAILED", e.getMessage());
                    throw new RuntimeException("Forgot password email failed");
                }
            }
        }
    }

    private String buildForgotPasswordTemplate(String userName, String otp) {
        String primaryColor = "#1a1a1a";
        String accentColor = "#c5a059";
        String textColor = "#444444";

        return "<div style='font-family: \"Georgia\", serif; max-width: 600px; margin: 0 auto; border: 1px solid #e0e0e0; background-color: #ffffff;'>" +

                "<div style='background-color: " + primaryColor + "; padding: 30px; text-align: center; border-bottom: 4px solid " + accentColor + ";'>" +
                "<h1 style='color: " + accentColor + "; margin: 0; font-size: 24px; text-transform: uppercase; letter-spacing: 2px;'>Hotel Management System</h1>" +
                "</div>" +

                "<div style='padding: 40px; text-align: center; color: " + textColor + ";'>" +
                "<h2 style='color: " + primaryColor + "; font-size: 22px;'>Password Reset Request</h2>" +
                "<p style='font-size: 16px;'>Dear " + userName + ",</p>" +
                "<p>We received a request to access your account. Please use the exclusive verification code below to proceed with your password reset.</p>" +

                "<div style='margin: 40px 0;'>" +
                "<div style='display: inline-block; padding: 20px 40px; border: 1px solid " + accentColor + "; background-color: #fafafa;'>" +
                "<span style='font-size: 32px; font-weight: bold; letter-spacing: 8px; color: " + primaryColor + ";'>" +
                otp +
                "</span>" +
                "</div>" +
                "<p style='color: " + accentColor + "; font-size: 12px; margin-top: 10px; text-transform: uppercase;'>Valid for 10 minutes only</p>" +
                "</div>" +

                "<p style='font-size: 14px; font-style: italic;'>If you did not make this request, please ignore this email or contact our concierge.</p>" +
                "</div>" +

                "<div style='background-color: #f9f9f9; padding: 20px; text-align: center; border-top: 1px solid #eeeeee; font-size: 12px; color: #888888;'>" +
                "<p>&copy; 2026 Hotel Management System | Azamgarh, Uttar Pradesh</p>" +
                "<p style='margin-bottom: 5px;'>Developed by <b>Manish Verma</b>  </p>"+
               "</div>" +
                "</div>";
    }


    public void sendBookingConfirmation(String toEmail, String userName, String hotelName, String roomType, double totalPrice) {

        String subject = "Booking Confirmed - " + hotelName;

        try {

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setFrom(FROM_EMAIL);
            helper.setSubject(subject);

            String html =
                    "<div style='font-family: \"Georgia\", serif; background-color: #f0f2f5; padding: 40px 10px;'>" +
                            "<div style='max-width: 500px; margin: 0 auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.1); border: 1px solid #e1e4e8;'>" +

                            "<div style='padding: 40px; color: #444; line-height: 1.6;'>" +

                            "<h2 style='color: #1a1a1a; margin-top: 0; font-size: 26px; text-align: center;'>Booking Confirmed</h2>" +
                            "<div style='width: 50px; height: 3px; background-color: #c5a059; margin: 15px auto;'></div>" +

                            "<p style='font-size: 16px; margin-top: 25px;'>Dear <b>" + userName + "</b>,</p>" +
                            "<p>Your reservation has been successfully processed. We look forward to welcoming you to our hotel.</p>" +

                            "<div style='margin: 30px 0; background-color: #f9f9f9; padding: 25px; border-radius: 8px; border-left: 5px solid #c5a059;'>" +
                            "<p style='margin: 5px 0; font-size: 14px; color: #777; text-transform: uppercase;'>Reservation Summary</p>" +
                            "<div style='margin-top: 15px;'>" +
                            "<p style='margin: 8px 0;'><b>Hotel:</b> " + hotelName + "</p>" +
                            "<p style='margin: 8px 0;'><b>Room Type:</b> " + roomType + "</p>" +
                            "<p style='margin: 8px 0;'><b>Booking ID:</b> #BK-" + (System.currentTimeMillis() % 100000) + "</p>" +
                            "<hr style='border: 0; border-top: 1px solid #eee; margin: 15px 0;' />" +
                            "<p style='margin: 0; font-size: 20px; color: #1a1a1a;'><b>Total Amount:</b> <span style='color: #2e7d32;'>₹" + String.format("%.2f", totalPrice) + "</span></p>" +
                            "</div>" +
                            "</div>" +

                            "<p style='font-size: 14px; color: #666; text-align: center;'>Please have this confirmation ready during check-in.</p>" +

                            "<div style='text-align: center; margin-top: 30px;'>" +
                            "<a href='#' style='background-color: #1a1a1a; color: #c5a059; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold; border: 1px solid #c5a059;'>View Booking Details</a>" +
                            "</div>" +
                            "</div>" +

                            "<div style='background-color: #f9f9f9; padding: 20px; text-align: center; border-top: 1px solid #eee; font-size: 12px; color: #888;'>" +
                            "<p style='margin: 5px 0;'>&copy; 2026 Hotel Management System | Azamgarh </p>" +
                            "<p style='margin: 5px 0;'>Developed by <b>Manish Verma</b></p>" +
                            "</div>" +
                            "</div>" +
                            "</div>";
            helper.setText(html, true);

            mailSender.send(message);

            log(toEmail, subject, "SUCCESS", null);

        } catch (Exception e) {
            log(toEmail, subject, "FAILED", e.getMessage());
        }
    }


    @Async
    public void sendWelcomeEmail(String toEmail, String userName) {
        String subject = "Welcome to " + APP_NAME + " - Your Luxury Stay Awaits";
        String exploreUrl = "http://localhost:3000/rooms";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setFrom(FROM_EMAIL);
            helper.setSubject(subject);

            String html = buildWelcomeTemplate(userName, exploreUrl);
            helper.setText(html, true);

            mailSender.send(message);
            log(toEmail, subject, "SUCCESS", null);

        } catch (Exception e) {
            log(toEmail, subject, "FAILED", e.getMessage());
        }
    }

    private String buildWelcomeTemplate(String userName, String exploreUrl) {
        String primaryColor = "#1a1a1a";
        String accentColor = "#c5a059";
        String secondaryBg = "#f9f9f9";

        return "<div style='font-family: \"Georgia\", serif; background-color: #f0f2f5; padding: 40px 10px;'>" +
                "<div style='max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.1); border: 1px solid #e1e4e8;'>" +

                "<div style='background-color: " + primaryColor + "; padding: 30px; text-align: center; border-bottom: 4px solid " + accentColor + ";'>" +
                "<h1 style='color: " + accentColor + "; margin: 0; font-size: 24px; text-transform: uppercase; letter-spacing: 2px;'>" + APP_NAME + "</h1>" +
                "</div>" +

                "<div style='padding: 40px; color: #444; line-height: 1.6;'>" +
                "<h2 style='color: " + primaryColor + "; margin-top: 0;'>Welcome aboard, " + userName + "!</h2>" +
                "<p style='font-size: 16px;'>We’re thrilled to have you with us. Whether you're planning a business trip, a family vacation, or just a quick getaway, we are here to make your stay unforgettable.</p>" +

                "<div style='margin: 25px 0; background-color: " + secondaryBg + "; padding: 20px; border-radius: 8px; border-left: 4px solid " + accentColor + ";'>" +
                "<p style='margin: 0 0 10px 0; font-weight: bold; color: " + primaryColor + ";'>What can you do now?</p>" +
                "<ul style='margin: 0; padding-left: 20px; font-size: 14px;'>" +
                "<li><b>Explore Rooms:</b> Browse through our luxury suites and cozy deluxe rooms.</li>" +
                "<li><b>Instant Booking:</b> Book your stay in just a few clicks.</li>" +
                "<li><b>Manage Trips:</b> View or reschedule bookings from your dashboard.</li>" +
                "</ul>" +
                "</div>" +

                "<div style='text-align: center; margin-top: 35px;'>" +
                "<a href='" + exploreUrl + "' style='background-color: " + primaryColor + "; color: " + accentColor + "; padding: 15px 30px; text-decoration: none; border-radius: 5px; font-weight: bold; border: 1px solid " + accentColor + "; display: inline-block;'>" +
                "EXPLORE THE SYSTEM</a>" +
                "</div>" +
                "</div>" +

                "<div style='background-color: " + secondaryBg + "; padding: 25px; text-align: center; border-top: 1px solid #eee; font-size: 12px; color: #888;'>" +
                "<p style='margin: 5px 0;'>&copy; 2026 " + APP_NAME + " | Azamgarh</p>" +
                "<p style='margin: 10px 0 0 0; color: #333;'>Developed by <b>Manish Verma</b></p>" +
                "</div>" +

                "</div>" +
                "</div>";
    }


    private void log(String to, String subject, String status, String error) {

        EmailLog log = new EmailLog();
        log.setToEmail(to);
        log.setSubject(subject);
        log.setStatus(status);
        log.setErrorMessage(error);
        log.setSentAt(LocalDateTime.now());

        logRepository.save(log);
    }
}