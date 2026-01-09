$backendPath = "c:\Users\INSIDER\Desktop\My Library\Websites\zetumall-backend"
$aiPath = "c:\Users\INSIDER\Desktop\My Library\Websites\zetumall-ai-service"

Write-Host "ZetuMall GitHub Setup Helper" -ForegroundColor Cyan
Write-Host "--------------------------------"

# Check directories
if (!(Test-Path $backendPath)) { Write-Error "Backend path not found: $backendPath"; exit }
if (!(Test-Path $aiPath)) { Write-Error "AI Service path not found: $aiPath"; exit }

# Backend Setup
Write-Host "`nSetting up Spring Boot Backend..." -ForegroundColor Yellow
$backendUrl = Read-Host "Enter your GitHub Repo URL for 'zetumall-backend' (e.g., https://github.com/user/zetumall-backend.git)"

if ($backendUrl) {
    Set-Location $backendPath
    git init
    git add .
    git commit -m "Initial commit of ZetuMall Spring Boot Backend" 2>$null
    git branch -M main
    
    # Remove existing remote if any
    git remote remove origin 2>$null
    
    git remote add origin $backendUrl
    Write-Host "Pushing backend code..." -ForegroundColor Cyan
    git push -u origin main
    
    if ($?) { Write-Host "Backend pushed successfully!" -ForegroundColor Green }
    else { Write-Host "Failed to push backend. Check URL and permissions." -ForegroundColor Red }
} else {
    Write-Host "Skipping backend setup." -ForegroundColor Gray
}

# AI Service Setup
Write-Host "`nSetting up Python AI Service..." -ForegroundColor Yellow
$aiUrl = Read-Host "Enter your GitHub Repo URL for 'zetumall-ai-service' (e.g., https://github.com/user/zetumall-ai-service.git)"

if ($aiUrl) {
    Set-Location $aiPath
    git init
    git add .
    git commit -m "Initial commit of ZetuMall AI Microservice" 2>$null
    git branch -M main
    
    # Remove existing remote if any
    git remote remove origin 2>$null
    
    git remote add origin $aiUrl
    Write-Host "Pushing AI service code..." -ForegroundColor Cyan
    git push -u origin main
    
    if ($?) { Write-Host "AI Service pushed successfully!" -ForegroundColor Green }
    else { Write-Host "Failed to push AI service. Check URL and permissions." -ForegroundColor Red }
} else {
    Write-Host "Skipping AI service setup." -ForegroundColor Gray
}

Write-Host "`nSetup Complete! You can now deploy these repos on Railway." -ForegroundColor Cyan
Pause
