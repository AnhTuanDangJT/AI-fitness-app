# Full Application Restart Script
# 
# This script restarts both the backend (Spring Boot) and frontend (React).
#
# Why we use tasklist/taskkill instead of netstat + findstr:
# - netstat + findstr can hang indefinitely in some environments (like Cursor)
# - netstat requires parsing port numbers which is error-prone
# - tasklist/taskkill directly targets processes by name, which is faster and more reliable
# - taskkill exits immediately with clear success/failure codes
# - No need to parse network connections when we just want to kill a process

Write-Host "=== Full Application Restart ===" -ForegroundColor Cyan
Write-Host ""

# ============================================
# Stop Backend (Java/Spring Boot)
# ============================================
Write-Host "--- Backend (Spring Boot) ---" -ForegroundColor Yellow
$javaProcesses = Get-Process -Name java -ErrorAction SilentlyContinue

if ($javaProcesses) {
    Write-Host "Found $($javaProcesses.Count) Java process(es) running" -ForegroundColor Yellow
    Write-Host "Killing old Java processes..." -ForegroundColor Yellow
    
    taskkill /F /IM java.exe 2>&1 | Out-Null
    Start-Sleep -Milliseconds 500
    
    $remaining = Get-Process -Name java -ErrorAction SilentlyContinue
    if ($remaining) {
        Write-Host "Warning: Some Java processes may still be running" -ForegroundColor Red
    } else {
        Write-Host "All Java processes terminated successfully" -ForegroundColor Green
    }
} else {
    Write-Host "No Java processes found - nothing to kill" -ForegroundColor Green
}

Write-Host ""

# ============================================
# Stop Frontend (Node.js)
# ============================================
Write-Host "--- Frontend (React/Node) ---" -ForegroundColor Yellow
$nodeProcesses = Get-Process -Name node -ErrorAction SilentlyContinue

if ($nodeProcesses) {
    Write-Host "Found $($nodeProcesses.Count) Node process(es) running" -ForegroundColor Yellow
    Write-Host "Killing old Node processes..." -ForegroundColor Yellow
    
    taskkill /F /IM node.exe 2>&1 | Out-Null
    Start-Sleep -Milliseconds 500
    
    $remaining = Get-Process -Name node -ErrorAction SilentlyContinue
    if ($remaining) {
        Write-Host "Warning: Some Node processes may still be running" -ForegroundColor Red
    } else {
        Write-Host "All Node processes terminated successfully" -ForegroundColor Green
    }
} else {
    Write-Host "No Node processes found - nothing to kill" -ForegroundColor Green
}

Write-Host ""
Write-Host "Waiting 2 seconds before restarting..." -ForegroundColor Yellow
Start-Sleep -Seconds 2
Write-Host ""

# ============================================
# Start Backend
# ============================================
Write-Host "--- Starting Backend ---" -ForegroundColor Yellow
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptPath

Write-Host "Starting Spring Boot backend..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "mvn spring-boot:run" -WindowStyle Normal

Write-Host "Backend starting in new window..." -ForegroundColor Green
Write-Host ""

# ============================================
# Start Frontend
# ============================================
Write-Host "--- Starting Frontend ---" -ForegroundColor Yellow
Set-Location "$scriptPath\client"

Write-Host "Starting React frontend..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "npm run dev" -WindowStyle Normal

Write-Host "Frontend starting in new window..." -ForegroundColor Green
Write-Host ""

# ============================================
# Summary
# ============================================
Write-Host "=== Restart Complete ===" -ForegroundColor Cyan
Write-Host "Backend: http://localhost:8080" -ForegroundColor Cyan
Write-Host "Frontend: http://localhost:5173" -ForegroundColor Cyan
Write-Host ""
Write-Host "Both services are starting in separate windows." -ForegroundColor Green
Write-Host "Check the windows for startup logs and any errors." -ForegroundColor Yellow
Write-Host ""

# Exit immediately
exit 0





