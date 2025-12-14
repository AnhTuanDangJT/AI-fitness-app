# Gmail SMTP Email Configuration

## Setup Complete ✅

The backend is now configured to send emails via Gmail SMTP.

## Files Created

1. **`.env`** - Contains Gmail SMTP credentials
2. **`start-backend.ps1`** - PowerShell script to load .env and start backend
3. **`start-backend.bat`** - Batch script to load .env and start backend

## Configuration Details

- **SMTP Host**: smtp.gmail.com
- **SMTP Port**: 587 (TLS)
- **Username**: dangtuanjt@gmail.com
- **From Email**: dangtuanjt@gmail.com

## How to Start Backend with Email Configuration

### Option 1: Use the PowerShell Script (Recommended)
```powershell
.\start-backend.ps1
```

### Option 2: Use the Batch Script
```cmd
start-backend.bat
```

### Option 3: Manual Setup
Set environment variables manually before starting:
```powershell
$env:MAIL_HOST="smtp.gmail.com"
$env:MAIL_PORT="587"
$env:MAIL_USERNAME="dangtuanjt@gmail.com"
$env:MAIL_PASSWORD="pqfh feau gzji jgso"
$env:APP_EMAIL_FROM="dangtuanjt@gmail.com"
mvn spring-boot:run
```

## Testing

1. Start the backend using one of the methods above
2. Register a new user via the signup endpoint
3. Check the user's email inbox for the verification code
4. Use the code to verify the email

## Note on Gmail App Password

The Gmail app password in `.env` includes spaces for readability. If email sending fails, try removing the spaces from the password:
- With spaces: `pqfh feau gzji jgso`
- Without spaces: `pqfhfeaugzjijgso`

## Verification

When the backend starts, check the logs for:
- ✅ `Email service is configured - Host: smtp.gmail.com, From: dangtuanjt@gmail.com`
- ❌ `Email service is NOT configured` (if credentials are missing)

