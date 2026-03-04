$ErrorActionPreference = "Continue"

Write-Host "Starting Zetumall Backend Setup..."

# 1. Setup Java
$JavaPath = "C:\Program Files\Microsoft\jdk-21.0.10.7-hotspot"
if (Test-Path $JavaPath) {
    Write-Host "Found Java 21 at: $JavaPath"
    $env:JAVA_HOME = $JavaPath
    $env:PATH = "$JavaPath\bin;$env:PATH"
}
else {
    Write-Warning "Could not find Java at $JavaPath. Proceeding with system default if available."
}

# 2. Load Environment Variables from .env
if (Test-Path ".env") {
    Write-Host "Loading environment variables from .env..."
    Get-Content .env -Encoding UTF8 | ForEach-Object {
        $trimmed = $_.Trim()
        if ($trimmed -and -not $trimmed.StartsWith("#") -and $trimmed.Contains("=")) {
            $key, $value = $trimmed -split "=", 2
            if ($key -and $value) {
                [System.Environment]::SetEnvironmentVariable($key.Trim(), $value.Trim())
                Write-Host "Set $key"
            }
        }
    }
}

# 3. Run
Write-Host "Starting Spring Boot Server..."
if (Test-Path ".\gradlew.bat") {
    Write-Host "Using Gradle Wrapper..."
    .\gradlew.bat bootRun
}
else {
    Write-Error "gradlew.bat not found. Please ensure you are in the project root."
    exit 1
}
