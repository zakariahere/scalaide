[CmdletBinding()]
param([string]$IdeHome = $env:SCALAIDE_IDEA_HOME)
$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
if (-not $IdeHome) {
    $installedHost = Join-Path $env:ProgramFiles 'JetBrains\IntelliJ IDEA 2026.2.1'
    if (Test-Path -LiteralPath (Join-Path $installedHost 'product-info.json')) { $IdeHome = $installedHost }
}
$gradleArguments = @('verify', '--console=plain')
if ($IdeHome) { $gradleArguments += "-PlocalIdePath=$IdeHome" }
Push-Location $repoRoot
try {
    & (Join-Path $repoRoot 'gradlew.bat') @gradleArguments
    if ($LASTEXITCODE -ne 0) { throw "Verification failed with code $LASTEXITCODE" }
} finally { Pop-Location }
