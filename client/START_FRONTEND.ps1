# Quick Frontend Start Script
# 
# This script starts the React frontend in a new window.
# You can see all the logs and stop it with Ctrl+C.

Write-Host "=== Starting Frontend ===" -ForegroundColor Cyan
Write-Host ""

# Get the script directory (client folder)
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptPath

Write-Host "Starting React frontend..." -ForegroundColor Yellow
Write-Host "Frontend will be available at: http://localhost:5173" -ForegroundColor Green
Write-Host ""
Write-Host "Press Ctrl+C in the new window to stop the frontend." -ForegroundColor Yellow
Write-Host ""

# Start in a new window so you can see the logs
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$scriptPath'; Write-Host 'Starting React Frontend...' -ForegroundColor Cyan; npm run dev"

Write-Host "Frontend starting in new window..." -ForegroundColor Green
Write-Host ""





