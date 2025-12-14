@echo off
REM Batch script to load .env file and start Spring Boot backend
REM Usage: start-backend.bat

echo Loading environment variables from .env file...

REM Load .env file and set environment variables
if exist .env (
    for /f "tokens=1,2 delims==" %%a in (.env) do (
        if not "%%a"=="" (
            if not "%%a"=="#" (
                set "%%a=%%b"
                echo   Set: %%a
            )
        )
    )
    echo Environment variables loaded successfully!
) else (
    echo Warning: .env file not found!
)

echo.
echo Starting Spring Boot backend...
call mvn spring-boot:run

