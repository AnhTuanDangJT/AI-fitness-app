# Quick Frontend Restart (Inline Version)
# 
# This version runs in the current window and shows npm output directly.
# Use this if you want to see the logs in the same terminal.
#
# Why we use tasklist/taskkill instead of netstat + findstr:
# - netstat + findstr can hang indefinitely in some environments (like Cursor)
# - netstat requires parsing port numbers which is error-prone  
# - tasklist/taskkill directly targets processes by name, which is faster and more reliable
# - taskkill exits immediately with clear success/failure codes
# - No need to parse network connections when we just want to kill a process

Write-Host "Killing old Node processes..." -ForegroundColor Yellow
taskkill /F /IM node.exe 2>&1 | Out-Null
Start-Sleep -Milliseconds 500

Write-Host "Starting frontend..." -ForegroundColor Green
Set-Location $PSScriptRoot
npm run dev






