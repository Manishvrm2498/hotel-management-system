# Hotel Management System Project
# hotel-management-system
# hotel_management_system
# hotel_management

## Render email configuration

The API sends registration and password-reset OTPs through Gmail SMTP. Configure
these secret environment variables in the Render service before deploying:

- `DEV_GMAIL`: the Gmail address used to send emails
- `DEV_PASSWORD`: a Google App Password for that account, not the normal Google
  account password

The Gmail account must have 2-Step Verification enabled before an App Password
can be created. After changing either secret, redeploy the Render service.
