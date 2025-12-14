# Email Configuration Guide

## Fixed Issues
✅ **EmailService.java** - Updated `setFrom()` to use a valid email address:
```java
message.setFrom("no-reply@aifitness.com");
```

## Required Configuration

### Environment Variables
The following environment variables must be set for email to work:

1. **MAIL_USERNAME** - Your Gmail address
   ```bash
   export MAIL_USERNAME=your-email@gmail.com
   ```
   Or in PowerShell:
   ```powershell
   $env:MAIL_USERNAME="your-email@gmail.com"
   ```

2. **MAIL_PASSWORD** - Gmail App Password (NOT your regular Gmail password)
   ```bash
   export MAIL_PASSWORD=your-16-character-app-password
   ```
   Or in PowerShell:
   ```powershell
   $env:MAIL_PASSWORD="your-16-character-app-password"
   ```

### How to Get Gmail App Password

1. Go to your Google Account: https://myaccount.google.com/
2. Navigate to **Security** → **2-Step Verification** (must be enabled)
3. Scroll down to **App passwords**
4. Select **Mail** and **Other (Custom name)**
5. Enter "AI Fitness App" as the name
6. Click **Generate**
7. Copy the 16-character password (no spaces)

### Alternative: Set in application.properties (NOT RECOMMENDED for production)

If you want to set it directly in `application.properties` (for local development only):

```properties
spring.mail.username=your-email@gmail.com
spring.mail.password=your-16-character-app-password
```

⚠️ **WARNING**: Never commit credentials to version control!

### Verification

After setting the environment variables and restarting the backend:

1. Try signup from the frontend
2. Check backend logs for:
   - `Verification email sent to: <email>`
   - Any email sending errors

3. Check your email inbox (and spam folder) for the verification code

### Current Configuration

The `application.properties` file is configured to read from environment variables:
```properties
spring.mail.host=${MAIL_HOST:smtp.gmail.com}
spring.mail.port=${MAIL_PORT:587}
spring.mail.username=${MAIL_USERNAME:}
spring.mail.password=${MAIL_PASSWORD:}
spring.mail.properties.mail.smtp.auth=${MAIL_SMTP_AUTH:true}
spring.mail.properties.mail.smtp.starttls.enable=${MAIL_SMTP_STARTTLS_ENABLE:true}
```

If environment variables are not set, these will be empty and email sending will fail.

## Testing

After configuration:
1. Restart the backend
2. Try creating a new account
3. Check your email for the verification code
4. Enter the code to verify your email

