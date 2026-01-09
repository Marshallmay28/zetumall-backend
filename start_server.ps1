$ErrorActionPreference = "Stop"

Write-Host "🚀 Starting Zetumall Backend Setup..." -ForegroundColor Cyan

# 1. Setup Java
$JavaPath = "C:\Program Files\Eclipse Adoptium\jdk-21.0.9.10-hotspot"
if (Test-Path $JavaPath) {
    Write-Host "✅ Found Java 21 at: $JavaPath" -ForegroundColor Green
    $env:JAVA_HOME = $JavaPath
    $env:PATH = "$JavaPath\bin;$env:PATH"
}
else {
    Write-Host "❌ Could not find Java at $JavaPath" -ForegroundColor Red
    Write-Host "Please install Java 21."
    exit 1
}

# 2. Setup Gradle (Local Install)
$GradleVersion = "8.5"
$GradleDir = "$PSScriptRoot\.gradle-local"
$GradleZip = "$GradleDir\gradle-$GradleVersion-bin.zip"
$GradleBin = "$GradleDir\gradle-$GradleVersion\bin"

if (-not (Test-Path "$GradleBin\gradle.bat")) {
    Write-Host "⚠️ Gradle not found. Downloading Gradle $GradleVersion..." -ForegroundColor Yellow
    New-Item -ItemType Directory -Force -Path $GradleDir | Out-Null
    
    # Download
    $ProgressPreference = 'SilentlyContinue'
    Invoke-WebRequest -Uri "https://services.gradle.org/distributions/gradle-$GradleVersion-bin.zip" -OutFile $GradleZip
    
    Write-Host "📦 Extracting Gradle..." -ForegroundColor Yellow
    Expand-Archive -Path $GradleZip -DestinationPath $GradleDir -Force
    
    # Cleanup
    Remove-Item $GradleZip
}

$env:PATH = "$GradleBin;$env:PATH"
Write-Host "✅ Gradle configured." -ForegroundColor Green

# 3. Clean and Run
Write-Host "🔥 Starting Spring Boot Server..." -ForegroundColor Cyan
Write-Host "⏳ This might take a minute locally..." -ForegroundColor Gray

# Run gradle bootRun
gradle.bat bootRun
