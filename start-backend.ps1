# PowerShell script to load .env file and start Spring Boot backend
# Usage: .\start-backend.ps1

Write-Host "Loading environment variables from .env file..." -ForegroundColor Cyan

# Load .env file
if (Test-Path .env) {
    Get-Content .env | ForEach-Object {
        if ($_ -match '^\s*([^#][^=]+)=(.*)$') {
            $key = $matches[1].Trim()
            $value = $matches[2].Trim()
            [Environment]::SetEnvironmentVariable($key, $value, "Process")
            Write-Host "  Set: $key" -ForegroundColor Green
        }
    }
    Write-Host "Environment variables loaded successfully!" -ForegroundColor Green
} else {
    Write-Host "Warning: .env file not found!" -ForegroundColor Yellow
}

Write-Host "`nStarting Spring Boot backend..." -ForegroundColor Cyan
mvn spring-boot:run

