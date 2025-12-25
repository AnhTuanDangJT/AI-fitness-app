# Email Health Check Script
# Tests the /api/health/email endpoint on production backend

$backendUrl = "https://web-production-4b668.up.railway.app"
$healthEndpoint = "$backendUrl/api/health/email"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Email Configuration Health Check" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Testing endpoint: $healthEndpoint" -ForegroundColor Yellow
Write-Host ""

try {
    $response = Invoke-RestMethod -Uri $healthEndpoint -Method Get -ErrorAction Stop
    
    Write-Host "✅ Health Check Successful" -ForegroundColor Green
    Write-Host ""
    Write-Host "Configuration Status:" -ForegroundColor Cyan
    Write-Host "  Email Configured: " -NoNewline
    if ($response.emailConfigured) {
        Write-Host "✅ TRUE" -ForegroundColor Green
    } else {
        Write-Host "❌ FALSE" -ForegroundColor Red
    }
    
    Write-Host "  Provider: " -NoNewline
    Write-Host $response.provider -ForegroundColor Yellow
    
    Write-Host ""
    Write-Host "Environment Variables Status:" -ForegroundColor Cyan
    Write-Host "  MAIL_HOST set: " -NoNewline
    if ($response.hostSet) {
        Write-Host "✅" -ForegroundColor Green
    } else {
        Write-Host "❌ MISSING" -ForegroundColor Red
    }
    
    Write-Host "  MAIL_USERNAME set: " -NoNewline
    if ($response.userSet) {
        Write-Host "✅" -ForegroundColor Green
    } else {
        Write-Host "❌ MISSING" -ForegroundColor Red
    }
    
    Write-Host "  MAIL_PASSWORD set: " -NoNewline
    if ($response.passSet) {
        Write-Host "✅" -ForegroundColor Green
    } else {
        Write-Host "❌ MISSING" -ForegroundColor Red
    }
    
    Write-Host "  APP_EMAIL_FROM set: " -NoNewline
    if ($response.fromSet) {
        Write-Host "✅" -ForegroundColor Green
    } else {
        Write-Host "❌ MISSING" -ForegroundColor Red
    }
    
    Write-Host ""
    Write-Host "Timestamp: $($response.timestamp)" -ForegroundColor Gray
    
    Write-Host ""
    if ($response.emailConfigured) {
        Write-Host "✅ Email service is properly configured!" -ForegroundColor Green
        Write-Host "   You can now test signup to verify email sending works." -ForegroundColor Green
    } else {
        Write-Host "❌ Email service is NOT configured!" -ForegroundColor Red
        Write-Host "   Please set the missing environment variables in Railway:" -ForegroundColor Yellow
        Write-Host "   - MAIL_HOST" -ForegroundColor Yellow
        Write-Host "   - MAIL_PORT" -ForegroundColor Yellow
        Write-Host "   - MAIL_USERNAME" -ForegroundColor Yellow
        Write-Host "   - MAIL_PASSWORD" -ForegroundColor Yellow
        Write-Host "   - APP_EMAIL_FROM" -ForegroundColor Yellow
    }
    
} catch {
    Write-Host "❌ Health Check Failed" -ForegroundColor Red
    Write-Host ""
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "Possible causes:" -ForegroundColor Yellow
    Write-Host "  - Backend is not deployed or not running" -ForegroundColor Yellow
    Write-Host "  - Backend URL is incorrect" -ForegroundColor Yellow
    Write-Host "  - Network connectivity issue" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan









