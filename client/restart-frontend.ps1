# Frontend Restart Script
# 
# This script safely stops and restarts the React frontend.
#
# Why we use tasklist/taskkill instead of netstat + findstr:
# - netstat + findstr can hang indefinitely in some environments (like Cursor)
# - netstat requires parsing port numbers which is error-prone
# - tasklist/taskkill directly targets processes by name, which is faster and more reliable
# - taskkill exits immediately with clear success/failure codes
# - No need to parse network connections when we just want to kill a process

Write-Host "=== Frontend Restart Script ===" -ForegroundColor Cyan
Write-Host ""

# Check if node.exe is running
Write-Host "Checking for running Node processes..." -ForegroundColor Yellow
$nodeProcesses = Get-Process -Name node -ErrorAction SilentlyContinue

if ($nodeProcesses) {
    Write-Host "Found $($nodeProcesses.Count) Node process(es) running" -ForegroundColor Yellow
    Write-Host "Killing old Node processes..." -ForegroundColor Yellow
    
    # Kill all node processes
    taskkill /F /IM node.exe 2>&1 | Out-Null
    
    # Wait a moment for processes to terminate
    Start-Sleep -Milliseconds 500
    
    # Verify they're gone
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
Write-Host "Starting frontend..." -ForegroundColor Yellow
Write-Host ""

# Change to client directory and start npm
Set-Location $PSScriptRoot

# Start npm in a new window so you can see the logs
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot'; npm run dev" -WindowStyle Normal

Write-Host "Frontend starting in background..." -ForegroundColor Green
Write-Host "Check http://localhost:5173 (or the port shown in the terminal)" -ForegroundColor Cyan
Write-Host ""
Write-Host "Script completed successfully!" -ForegroundColor Green

# Exit immediately - don't wait for npm to finish
exit 0

