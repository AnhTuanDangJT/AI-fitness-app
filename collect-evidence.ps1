# PowerShell Script to Collect Runtime Evidence
# Run this script to collect all evidence for Verify Email error

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "RUNTIME EVIDENCE COLLECTION SCRIPT" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Check if backend is running
Write-Host "STEP 1: Checking Backend Status..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/health" -Method GET -TimeoutSec 5 -ErrorAction Stop
    Write-Host "✅ Backend is running" -ForegroundColor Green
    Write-Host "   Status: $($response.StatusCode)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Backend is NOT running or not accessible" -ForegroundColor Red
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Gray
    Write-Host "   Please start the backend first!" -ForegroundColor Yellow
}
Write-Host ""

# Step 2: Check frontend API base URL
Write-Host "STEP 2: Checking Frontend Configuration..." -ForegroundColor Yellow
$envFile = "client\.env"
$envLocalFile = "client\.env.local"
$viteConfig = "client\vite.config.js"

if (Test-Path $envFile) {
    Write-Host "✅ Found .env file" -ForegroundColor Green
    Get-Content $envFile | Where-Object { $_ -match "VITE_API_BASE_URL" } | ForEach-Object {
        Write-Host "   $_" -ForegroundColor Gray
    }
} elseif (Test-Path $envLocalFile) {
    Write-Host "✅ Found .env.local file" -ForegroundColor Green
    Get-Content $envLocalFile | Where-Object { $_ -match "VITE_API_BASE_URL" } | ForEach-Object {
        Write-Host "   $_" -ForegroundColor Gray
    }
} else {
    Write-Host "⚠️  No .env file found - using default from vite.config.js" -ForegroundColor Yellow
    if (Test-Path $viteConfig) {
        $config = Get-Content $viteConfig -Raw
        if ($config -match "target:\s*['`"]([^'`"]+)['`"]") {
            Write-Host "   Proxy target: $($matches[1])" -ForegroundColor Gray
        }
    }
}
Write-Host ""

# Step 3: Check backend logs
Write-Host "STEP 3: Checking Backend Logs..." -ForegroundColor Yellow
$logFile = "logs\spring-boot.log"
if (Test-Path $logFile) {
    Write-Host "✅ Log file exists" -ForegroundColor Green
    Write-Host "   Path: $logFile" -ForegroundColor Gray
    
    # Get last 50 lines
    $lastLines = Get-Content $logFile -Tail 50
    Write-Host ""
    Write-Host "   Last 50 lines of log:" -ForegroundColor Cyan
    Write-Host "   " + ("-" * 56) -ForegroundColor Gray
    $lastLines | ForEach-Object {
        Write-Host "   $_" -ForegroundColor Gray
    }
    Write-Host "   " + ("-" * 56) -ForegroundColor Gray
    
    # Search for email service config
    $allLogs = Get-Content $logFile
    $emailConfigLogs = $allLogs | Where-Object { 
        $_ -match "Email service" -or 
        $_ -match "MAIL_" -or 
        $_ -match "email.*configure" 
    }
    
    if ($emailConfigLogs) {
        Write-Host ""
        Write-Host "   Email service configuration logs:" -ForegroundColor Cyan
        $emailConfigLogs | Select-Object -Last 5 | ForEach-Object {
            Write-Host "   $_" -ForegroundColor Gray
        }
    }
} else {
    Write-Host "❌ Log file does not exist" -ForegroundColor Red
    Write-Host "   Path: $logFile" -ForegroundColor Gray
    Write-Host "   Make sure backend is running and logging to file" -ForegroundColor Yellow
}
Write-Host ""

# Step 4: Check database
Write-Host "STEP 4: Checking Database..." -ForegroundColor Yellow
$dbFile = "aifitness.db"
if (Test-Path $dbFile) {
    Write-Host "✅ Database file exists" -ForegroundColor Green
    Write-Host "   Path: $dbFile" -ForegroundColor Gray
    Write-Host ""
    Write-Host "   To check database, run:" -ForegroundColor Cyan
    Write-Host "   sqlite3 $dbFile" -ForegroundColor White
    Write-Host ""
    Write-Host "   Then run this query:" -ForegroundColor Cyan
    Write-Host "   SELECT email, is_email_verified, email_verification_code, verification_attempts" -ForegroundColor White
    Write-Host "   FROM users WHERE email = 'dangtuananh04081972@gmail.com';" -ForegroundColor White
} else {
    Write-Host "❌ Database file does not exist" -ForegroundColor Red
    Write-Host "   Path: $dbFile" -ForegroundColor Gray
    Write-Host "   Database might be in a different location or using PostgreSQL" -ForegroundColor Yellow
}
Write-Host ""

# Step 5: Make test API call
Write-Host "STEP 5: Making Test API Call..." -ForegroundColor Yellow
Write-Host "   This will make a verify-email request to capture network trace" -ForegroundColor Gray
Write-Host ""

$testEmail = "dangtuananh04081972@gmail.com"
$testCode = "123456"
$apiUrl = "http://localhost:8080/api/auth/verify-email"

$body = @{
    email = $testEmail
    code = $testCode
} | ConvertTo-Json

Write-Host "   Request URL: $apiUrl" -ForegroundColor Gray
Write-Host "   Method: POST" -ForegroundColor Gray
Write-Host "   Payload: $body" -ForegroundColor Gray
Write-Host ""

try {
    $response = Invoke-RestMethod -Uri $apiUrl -Method POST -Body $body -ContentType "application/json" -ErrorAction Stop
    Write-Host "✅ Request successful" -ForegroundColor Green
    Write-Host "   Status Code: 200" -ForegroundColor Gray
    Write-Host "   Response:" -ForegroundColor Gray
    $response | ConvertTo-Json -Depth 10 | ForEach-Object { Write-Host "   $_" -ForegroundColor Gray }
} catch {
    Write-Host "❌ Request failed" -ForegroundColor Red
    if ($_.Exception.Response) {
        $statusCode = [int]$_.Exception.Response.StatusCode.value__
        Write-Host "   Status Code: $statusCode" -ForegroundColor Gray
        
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "   Response Body:" -ForegroundColor Gray
        Write-Host "   $responseBody" -ForegroundColor Gray
    } else {
        Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Gray
    }
}
Write-Host ""

# Summary
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "NEXT STEPS:" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "1. Open browser DevTools (F12) → Network tab" -ForegroundColor White
Write-Host "2. Navigate to verify-email page" -ForegroundColor White
Write-Host "3. Enter code and click Verify" -ForegroundColor White
Write-Host "4. Capture the verify-email request details" -ForegroundColor White
Write-Host "5. Check backend console for exception stack traces" -ForegroundColor White
Write-Host "6. Run database queries (see collect-database-evidence.sql)" -ForegroundColor White
Write-Host ""

