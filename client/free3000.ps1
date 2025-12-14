# PowerShell script to free port 3000 and start the dev server

# Step 1: Check if port 3000 is in use
$connections = Get-NetTCPConnection -LocalPort 3000 -ErrorAction SilentlyContinue

# Step 2: If nothing is using the port
if ($null -eq $connections -or $connections.Count -eq 0) {
    Write-Host "Port 3000 is free" -ForegroundColor Green
} else {
    # Step 3: If something is using it
    Write-Host "Found process on port 3000" -ForegroundColor Yellow
    
    # Get unique process IDs (in case multiple connections from same process)
    $pids = $connections | Select-Object -ExpandProperty OwningProcess -Unique
    
    foreach ($pid in $pids) {
        Write-Host "PID: $pid" -ForegroundColor Yellow
        
        # Kill the process
        try {
            Stop-Process -Id $pid -Force
            Write-Host "Process $pid has been terminated" -ForegroundColor Green
        } catch {
            Write-Host "Failed to terminate process $pid : $_" -ForegroundColor Red
        }
    }
}

# Step 4: Start the dev server
Write-Host "Starting dev server..." -ForegroundColor Cyan
npm run dev

