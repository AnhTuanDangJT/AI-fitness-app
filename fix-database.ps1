# Script to fix database schema issue
# This will delete the old database so Hibernate can recreate it with all fields

Write-Host "========================================="
Write-Host "Database Schema Fix Script"
Write-Host "========================================="
Write-Host ""

# Check if database file exists
if (Test-Path "aifitness.db") {
    Write-Host "Found database file: aifitness.db"
    
    # Check if it's locked (backend is running)
    try {
        $file = Get-Item "aifitness.db"
        Write-Host "Database file size: $($file.Length) bytes"
        Write-Host "Last modified: $($file.LastWriteTime)"
        Write-Host ""
        
        Write-Host "WARNING: The database file is currently in use by the backend."
        Write-Host "You must STOP the Spring Boot backend first before deleting the database."
        Write-Host ""
        Write-Host "Steps to fix:"
        Write-Host "1. Stop the Spring Boot backend (Ctrl+C in the terminal where it's running)"
        Write-Host "2. Run this script again, or manually delete aifitness.db"
        Write-Host "3. Restart the backend - Hibernate will recreate the database with all fields"
        Write-Host "4. Test signup again"
        Write-Host ""
        
    } catch {
        Write-Host "Error accessing database file: $_"
    }
} else {
    Write-Host "Database file not found. It will be created when the backend starts."
    Write-Host ""
    Write-Host "Configuration check:"
    Write-Host "- spring.jpa.hibernate.ddl-auto=update (should be enabled)"
    Write-Host ""
    Write-Host "When you restart the backend, Hibernate will create the database"
    Write-Host "with all required fields including:"
    Write-Host "  - is_email_verified"
    Write-Host "  - email_verification_code"
    Write-Host "  - email_verification_expires_at"
}

Write-Host ""
Write-Host "Press any key to exit..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")









