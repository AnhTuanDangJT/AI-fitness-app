# Quick Backend Start Script
# 
# This script starts the Spring Boot backend in a new window.
# You can see all the logs and stop it with Ctrl+C.

Write-Host "=== Starting Backend ===" -ForegroundColor Cyan
Write-Host ""

# Get the script directory (project root)
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptPath

Write-Host "Starting Spring Boot backend..." -ForegroundColor Yellow
Write-Host "Backend will be available at: http://localhost:8080" -ForegroundColor Green
Write-Host ""
Write-Host "Press Ctrl+C in the new window to stop the backend." -ForegroundColor Yellow
Write-Host ""

# Start in a new window so you can see the logs
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$scriptPath'; Write-Host 'Starting Spring Boot Backend...' -ForegroundColor Cyan; mvn spring-boot:run"

Write-Host "Backend starting in new window..." -ForegroundColor Green
Write-Host ""













