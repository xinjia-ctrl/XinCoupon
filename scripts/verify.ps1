Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
Push-Location $repoRoot

try {
    Write-Host "[1/3] Run unit tests"
    mvn test

    Write-Host "[2/3] Scan high-risk secrets"
    $scanTargets = @("README.md", "docs", "src/main/resources")
    $patterns = @(
        "spring\.data\.redis\.password=[^<\s][^\s]+",
        "redis\.password=[A-Za-z0-9+/=]{20,}",
        "MYSQL_PASSWORD=[^<\s][^\s]+",
        "AUTH_ADMIN_TOKEN=[^<\s][^\s]+"
    )
    foreach ($pattern in $patterns) {
        $matches = rg --line-number --regexp $pattern $scanTargets
        if ($LASTEXITCODE -eq 0) {
            Write-Host $matches
            throw "Found high-risk secret pattern: $pattern"
        }
        if ($LASTEXITCODE -gt 1) {
            throw "Secret scan failed for pattern: $pattern"
        }
    }

    Write-Host "[3/3] Check git working tree"
    git status --short

    Write-Host "Verify finished"
} finally {
    Pop-Location
}
