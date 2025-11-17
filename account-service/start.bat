@echo off
REM Quick Start Script for Account Service

echo ====================================
echo Account Service - Quick Start
echo ====================================
echo.

REM Check if Docker is running
docker version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker is not running. Please start Docker Desktop first.
    pause
    exit /b 1
)

echo [INFO] Docker is running...
echo.

REM Ask user which startup mode
echo Select startup mode:
echo 1. Start with Docker Compose (Recommended)
echo 2. Build and start locally with Maven
echo 3. Run tests only
echo.
set /p choice="Enter your choice (1-3): "

if "%choice%"=="1" (
    echo.
    echo [INFO] Starting services with Docker Compose...
    docker-compose up -d
    echo.
    echo [SUCCESS] Services started!
    echo.
    echo Access:
    echo - Service: http://localhost:9001
    echo - Swagger: http://localhost:9001/swagger-ui.html
    echo - Health: http://localhost:9001/actuator/health
    echo.
    echo To view logs: docker-compose logs -f account-service
    echo To stop: docker-compose down
) else if "%choice%"=="2" (
    echo.
    echo [INFO] Building with Maven...
    call mvn clean package -DskipTests
    if errorlevel 1 (
        echo [ERROR] Build failed!
        pause
        exit /b 1
    )
    echo.
    echo [INFO] Starting application...
    start "Account Service" java -jar target\account-service-1.0.0.jar
    echo.
    echo [SUCCESS] Application started in new window!
    echo.
    echo Access:
    echo - Service: http://localhost:9001
    echo - Swagger: http://localhost:9001/swagger-ui.html
) else if "%choice%"=="3" (
    echo.
    echo [INFO] Running tests...
    call mvn clean test
    echo.
    echo [INFO] Generating coverage report...
    call mvn jacoco:report
    echo.
    echo [SUCCESS] Tests completed!
    echo Coverage report: target\site\jacoco\index.html
) else (
    echo [ERROR] Invalid choice!
)

echo.
pause
