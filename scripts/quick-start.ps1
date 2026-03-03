# Quick Start Script for Shopping Service Demo
# This script builds and starts all services

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Shopping Service - Quick Start" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Build all services
Write-Host "[1/3] Building all services with Maven..." -ForegroundColor Yellow
mvn clean package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Maven build failed!" -ForegroundColor Red
    exit 1
}
Write-Host "✅ Build successful!" -ForegroundColor Green
Write-Host ""

# Step 2: Build and start Docker containers
Write-Host "[2/3] Building and starting Docker containers..." -ForegroundColor Yellow
docker compose up -d --build
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Docker compose failed!" -ForegroundColor Red
    exit 1
}
Write-Host "✅ Containers started!" -ForegroundColor Green
Write-Host ""

# Step 3: Wait for services to be ready
Write-Host "[3/3] Waiting for services to initialize (30 seconds)..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

# Check status
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Service Status" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
docker compose ps

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host " ✅ Setup Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Access Points:" -ForegroundColor Cyan
Write-Host "  • Gateway:         http://localhost:8080" -ForegroundColor White
Write-Host "  • Auth Service:    http://localhost:9000/swagger-ui.html" -ForegroundColor White
Write-Host "  • Account Service: http://localhost:9001/swagger-ui.html" -ForegroundColor White
Write-Host "  • Item Service:    http://localhost:9002/swagger-ui.html" -ForegroundColor White
Write-Host "  • Inventory:       http://localhost:9003/swagger-ui.html" -ForegroundColor White
Write-Host "  • Order Service:   http://localhost:9004/swagger-ui.html" -ForegroundColor White
Write-Host "  • Payment Service: http://localhost:9005/swagger-ui.html" -ForegroundColor White
Write-Host "  • Kafka UI:        http://localhost:8082" -ForegroundColor White
Write-Host ""
Write-Host "To view logs: docker compose logs -f" -ForegroundColor Yellow
Write-Host "To stop all:  docker compose down" -ForegroundColor Yellow
Write-Host ""
