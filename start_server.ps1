$ErrorActionPreference = "Stop"

Write-Host "Starting Zetumall Backend Setup..."

# 1. Setup Java
$JavaPath = "C:\Program Files\Eclipse Adoptium\jdk-21.0.9.10-hotspot"
if (Test-Path $JavaPath) {
    Write-Host "Found Java 21 at: $JavaPath"
    $env:JAVA_HOME = $JavaPath
    $env:PATH = "$JavaPath\bin;$env:PATH"
}
else {
    Write-Host "Could not find Java at $JavaPath"
    Write-Host "Please install Java 21."
    exit 1
}

# 2. Setup Gradle (Local Install)
$GradleVersion = "8.5"
$GradleDir = "$PSScriptRoot\.gradle-local"
$GradleZip = "$GradleDir\gradle-$GradleVersion-bin.zip"
$GradleBin = "$GradleDir\gradle-$GradleVersion\bin"

if (-not (Test-Path "$GradleBin\gradle.bat")) {
    Write-Host "Gradle not found. Downloading Gradle $GradleVersion..."
    New-Item -ItemType Directory -Force -Path $GradleDir | Out-Null
    
    # Download
    $ProgressPreference = 'SilentlyContinue'
    Invoke-WebRequest -Uri "https://services.gradle.org/distributions/gradle-$GradleVersion-bin.zip" -OutFile $GradleZip
    
    Write-Host "Extracting Gradle..."
    Expand-Archive -Path $GradleZip -DestinationPath $GradleDir -Force
    
    # Cleanup
    Remove-Item $GradleZip
}

$env:PATH = "$GradleBin;$env:PATH"
Write-Host "Gradle configured."

# 3. Load Environment Variables from .env
if (Test-Path ".env") {
    Write-Host "Loading environment variables from .env..."
    Get-Content .env | ForEach-Object {
        $trimmed = $_.Trim()
        if ($trimmed -and -not $trimmed.StartsWith("#") -and $trimmed.Contains("=")) {
            $key, $value = $trimmed -split "=", 2
            if ($key -and $value) {
                [System.Environment]::SetEnvironmentVariable($key.Trim(), $value.Trim())
            }
        }
    }
} else {
    Write-Warning ".env file not found. Ensure environment variables are set externally."
}

# 4. Clean and Run
Write-Host "Starting Spring Boot Server..."
Write-Host "This might take a minute locally..."

# Run gradle bootRun
& "$GradleBin\gradle.bat" bootRun
