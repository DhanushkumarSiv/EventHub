param(
    [string]$EnvFile = (Join-Path (Split-Path $PSScriptRoot -Parent) ".env")
)

if (-not (Test-Path $EnvFile)) {
    Write-Error "Missing $EnvFile. Copy .env.example to .env and set DB_PASSWORD."
    exit 1
}

Get-Content $EnvFile | ForEach-Object {
    $line = $_.Trim()
    if ($line -eq "" -or $line.StartsWith("#")) {
        return
    }
    $parts = $line -split "=", 2
    if ($parts.Length -ne 2) {
        return
    }
    $name = $parts[0].Trim()
    $value = $parts[1].Trim().Trim("'").Trim('"')
    Set-Item -Path "Env:$name" -Value $value
}

Write-Host "Loaded environment from $EnvFile"
