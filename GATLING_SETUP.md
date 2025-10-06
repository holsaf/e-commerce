# Gatling Load Testing - Quick Guide

## Run Gatling Test

```powershell
# Option 1: Automated script (easiest)
.\run-gatling-test.ps1

# Option 2: Interactive - Choose simulation from list
.\gradlew gatlingRun

# Option 3: Direct - Run specific simulation
.\gradlew gatlingRun --simulation=com.ecommerce.backend.simulations.ProductLoadSimulation
```

## View Results

Open the HTML report:
```
build/reports/gatling/<simulation-name>-<timestamp>/index.html
```

## Troubleshooting

**Error: "ClassNotFoundException"**
- Use full package name or run interactively: `.\gradlew gatlingRun`

**Error: "No simulations to run"**
```powershell
.\gradlew clean
.\gradlew gatlingClasses
.\gradlew gatlingRun
```

**Application not running?**
```powershell
.\gradlew bootRun
```

## Alternative: PowerShell Script

No Gatling/Scala needed:
```powershell
powershell -ExecutionPolicy Bypass -File .\simple-load-test.ps1
```

