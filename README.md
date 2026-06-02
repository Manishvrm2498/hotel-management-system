# Hotel Management System Project
# hotel-management-system
# hotel_management_system
# hotel_management

## Render email configuration

The API sends transactional emails through the Brevo HTTPS API so that it works
on Render's free plan, which blocks outbound SMTP ports. Configure these
environment variables in the Render service before deploying:

- `BREVO_API_KEY`: API key created in Brevo under SMTP & API > API Keys
- `BREVO_SENDER_EMAIL`: sender email address verified in Brevo
- `BREVO_SENDER_NAME`: optional sender name, defaults to `Hotel Management System`
- `FRONTEND_URL`: deployed frontend URL, used in email links

After changing these values, redeploy the Render service.
