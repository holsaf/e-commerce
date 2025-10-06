# Gatling Load Test Setup and Execution Script
# This script sets up Scala compilation and runs Gatling performance tests

param(
    [Parameter(Mandatory=$false)]
    [string]$BaseUrl = "http://localhost:8080",
    
    [Parameter(Mandatory=$false)]
    [string]$Simulation = "com.ecommerce.backend.simulations.ProductLoadSimulation"
)

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "GATLING LOAD TEST SETUP" -ForegroundColor Green
Write-Host "========================================`n" -ForegroundColor Cyan

# Step 1: Check if application is running
Write-Host "Step 1: Checking if application is running..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$BaseUrl/api/products" -UseBasicParsing -ErrorAction Stop
    Write-Host "   Application is running at $BaseUrl" -ForegroundColor Green
} catch {
    Write-Host "   ERROR: Application is NOT running!" -ForegroundColor Red
    Write-Host "   Please start the application first:" -ForegroundColor Yellow
    Write-Host "   .\gradlew bootRun`n" -ForegroundColor White
    exit 1
}

# Step 2: Compile Gatling simulations
Write-Host "`nStep 2: Compiling Gatling simulations..." -ForegroundColor Yellow
$null = & .\gradlew gatlingClasses 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "   Gatling simulations compiled successfully!" -ForegroundColor Green
} else {
    Write-Host "   ERROR: Compilation failed!" -ForegroundColor Red
    Write-Host "   Please check the error messages above.`n" -ForegroundColor Yellow
    exit 1
}

# Step 3: Run Gatling test
Write-Host "`nStep 3: Running Gatling simulation: $Simulation..." -ForegroundColor Yellow
Write-Host "   Base URL: $BaseUrl" -ForegroundColor Gray
Write-Host "   This may take a few minutes...`n" -ForegroundColor Gray

$env:BASE_URL = $BaseUrl

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "STARTING GATLING TEST" -ForegroundColor Green
Write-Host "========================================`n" -ForegroundColor Cyan

& .\gradlew gatlingRun --simulation=$Simulation

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n========================================" -ForegroundColor Cyan
    Write-Host "GATLING TEST COMPLETED SUCCESSFULLY!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "`nResults should be available in:" -ForegroundColor Yellow
    Write-Host "  build/reports/gatling/`n" -ForegroundColor Cyan
} else {
    Write-Host "`n========================================" -ForegroundColor Cyan
    Write-Host "GATLING TEST FAILED" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "`nIf you see 'No simulations to run', use the PowerShell alternative:" -ForegroundColor Yellow
    Write-Host "  powershell -ExecutionPolicy Bypass -File .\simple-load-test.ps1`n" -ForegroundColor Cyan
    exit 1
}
