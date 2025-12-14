# Manual Start Guide

This guide shows you how to manually start the backend and frontend services.

## Prerequisites

- **Java 17+** installed (check with `java -version`)
- **Maven** installed (check with `mvn --version`)
- **Node.js** installed (check with `node --version`)
- **npm** installed (check with `npm --version`)

## Option 1: Using Two Separate Terminal Windows (Recommended)

### Terminal 1: Backend (Spring Boot)

1. Open PowerShell or Command Prompt
2. Navigate to the project root:
   ```powershell
   cd C:\Users\Admin\Desktop\AI-fitness-app-main
   ```
3. Start the backend:
   ```powershell
   mvn spring-boot:run
   ```
4. Wait for the backend to start. You should see:
   ```
   Started AiFitnessApplication in X.XXX seconds
   ```
5. Backend will be running at: **http://localhost:8080**

### Terminal 2: Frontend (React/Vite)

1. Open a **NEW** PowerShell or Command Prompt window
2. Navigate to the client directory:
   ```powershell
   cd C:\Users\Admin\Desktop\AI-fitness-app-main\client
   ```
3. Start the frontend:
   ```powershell
   npm run dev
   ```
4. Wait for the frontend to start. You should see:
   ```
   VITE v5.x.x  ready in XXX ms
   ➜  Local:   http://localhost:5173/
   ```
5. Frontend will be running at: **http://localhost:5173**

---

## Option 2: Using Background Jobs (PowerShell)

### Start Backend in Background

```powershell
cd C:\Users\Admin\Desktop\AI-fitness-app-main
Start-Job -ScriptBlock { mvn spring-boot:run }
```

### Start Frontend in Background

```powershell
cd C:\Users\Admin\Desktop\AI-fitness-app-main\client
Start-Job -ScriptBlock { npm run dev }
```

**Note:** Background jobs won't show output. Use Option 1 if you want to see logs.

---

## Option 3: Using the Provided Scripts

### Restart Frontend Only
```powershell
cd client
.\restart-frontend.ps1
```

### Restart Both Backend and Frontend
```powershell
.\restart-all.ps1
```

---

## Stopping the Services

### Stop Backend (Java)
Press `Ctrl + C` in the backend terminal, or:
```powershell
taskkill /F /IM java.exe
```

### Stop Frontend (Node)
Press `Ctrl + C` in the frontend terminal, or:
```powershell
taskkill /F /IM node.exe
```

---

## Troubleshooting

### Backend won't start
- Check if port 8080 is already in use:
  ```powershell
  netstat -ano | findstr :8080
  ```
- Make sure Java 17+ is installed: `java -version`
- Make sure Maven is installed: `mvn --version`

### Frontend won't start
- Check if port 5173 is already in use:
  ```powershell
  netstat -ano | findstr :5173
  ```
- Make sure Node.js is installed: `node --version`
- Try deleting `node_modules` and reinstalling:
  ```powershell
  cd client
  Remove-Item -Recurse -Force node_modules
  npm install
  npm run dev
  ```

### "Port already in use" error
Kill the process using the port:
```powershell
# Find the process ID (PID) from netstat output
netstat -ano | findstr :8080
# Kill it (replace PID with actual number)
taskkill /F /PID <PID>
```

---

## Quick Reference

| Service | Command | Port | Directory |
|---------|---------|------|-----------|
| Backend | `mvn spring-boot:run` | 8080 | Root directory |
| Frontend | `npm run dev` | 5173 | `client/` directory |

---

## Expected Output

### Backend Success:
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.0)

... (logs) ...

Started AiFitnessApplication in 3.456 seconds
```

### Frontend Success:
```
  VITE v5.0.8  ready in 234 ms

  ➜  Local:   http://localhost:5173/
  ➜  Network: use --host to expose
  ➜  press h + enter to show help
```

---

## Tips

1. **Keep both terminals open** - You need both services running simultaneously
2. **Watch the logs** - Both terminals will show useful information and errors
3. **Stop with Ctrl+C** - This is the cleanest way to stop each service
4. **Check the ports** - Make sure nothing else is using ports 8080 or 5173





