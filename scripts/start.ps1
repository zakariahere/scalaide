[CmdletBinding()]
param(
    [string]$ProjectPath,
    [string]$IdeHome = $env:SCALAIDE_IDEA_HOME
)
$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
if (-not $ProjectPath) { $ProjectPath = Join-Path $repoRoot 'examples\hello-scala' }
$projectRoot = (Resolve-Path -LiteralPath $ProjectPath).Path
if (-not (Test-Path -LiteralPath $projectRoot -PathType Container)) { throw 'ProjectPath must be a directory.' }

if (-not $IdeHome) {
    $installedHost = Join-Path $env:ProgramFiles 'JetBrains\IntelliJ IDEA 2026.2.1'
    if (Test-Path -LiteralPath (Join-Path $installedHost 'product-info.json')) { $IdeHome = $installedHost }
}
$gradleArguments = @(':workbench:runIde', "-PscalaProject=$projectRoot", '--console=plain')
if ($IdeHome) {
    $hostInfo = Get-Content -LiteralPath (Join-Path $IdeHome 'product-info.json') -Raw | ConvertFrom-Json
    if ($hostInfo.version -ne '2026.2.1') { throw "Expected IntelliJ 2026.2.1; found $($hostInfo.version). See docs/adr/0001-platform.md." }
    $gradleArguments += "-PlocalIdePath=$IdeHome"
}
Write-Host "Starting Scala Workbench for $projectRoot"
Write-Host 'The first launch resolves dependencies. IDE configuration is kept in .intellijPlatform/sandbox.'
Push-Location $repoRoot
try {
    & (Join-Path $repoRoot 'gradlew.bat') @gradleArguments
    if ($LASTEXITCODE -ne 0) { throw "Development IDE exited with code $LASTEXITCODE" }
} finally { Pop-Location }
