$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot
Set-Location $root

function Find-Jdk {
    $candidates = New-Object System.Collections.Generic.List[string]
    if ($env:JAVA_HOME) { $candidates.Add($env:JAVA_HOME) }
    $candidates.Add('C:\Program Files\Eclipse Adoptium\jdk-21.0.13.9-hotspot')
    $candidates.Add('C:\Program Files\Eclipse Adoptium\jdk-25.0.0.36-hotspot')
    $candidates.Add('C:\Program Files\Java\jdk-21')
    $candidates.Add('C:\Program Files\Java\jdk-25')
    foreach ($candidate in $candidates) {
        if ($candidate -and (Test-Path (Join-Path $candidate 'bin\javac.exe'))) {
            return $candidate
        }
    }
    $command = Get-Command javac.exe -ErrorAction SilentlyContinue
    if ($command) {
        return (Split-Path (Split-Path $command.Source -Parent) -Parent)
    }
    throw 'JDK 21+ not found. Install Temurin JDK 21 or set JAVA_HOME.'
}

$jdk = Find-Jdk
$javac = Join-Path $jdk 'bin\javac.exe'
$java = Join-Path $jdk 'bin\java.exe'
$jar = Join-Path $jdk 'bin\jar.exe'
$jpackage = Join-Path $jdk 'bin\jpackage.exe'

Write-Host "Using JDK: $jdk"

$running = Get-Process IronVault -ErrorAction SilentlyContinue
if ($running) {
    throw 'IronVault is running. Close it before building.'
}

$classes = Join-Path $root 'out\classes'
Remove-Item -Recurse -Force $classes -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path $classes | Out-Null

$sources = Get-ChildItem -Recurse -Filter *.java (Join-Path $root 'src\main\java') |
        ForEach-Object FullName
& $javac -encoding UTF-8 -d $classes $sources
if ($LASTEXITCODE -ne 0) { throw 'Compilation failed.' }

$dist = Join-Path $root 'dist'
New-Item -ItemType Directory -Force -Path $dist | Out-Null

$jarFile = Join-Path $dist 'IronVault.jar'
& $jar --create --file $jarFile --main-class com.ironvault.IronVaultApp -C $classes .
if ($LASTEXITCODE -ne 0) { throw 'JAR packaging failed.' }
Write-Host "JAR: $jarFile"

& $javac -encoding UTF-8 -cp $classes -d $classes (Join-Path $root 'tools\MakeIcon.java')
if ($LASTEXITCODE -ne 0) { throw 'Icon compilation failed.' }
& $java -cp $classes MakeIcon (Join-Path $dist 'IronVault.ico')
if ($LASTEXITCODE -ne 0) { throw 'Icon generation failed.' }

$inputDir = Join-Path $root 'out\jpackage-input'
Remove-Item -Recurse -Force $inputDir -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path $inputDir | Out-Null
Copy-Item $jarFile $inputDir

$appDir = Join-Path $dist 'IronVault'
$vaultBackup = Join-Path $root 'out\vault-backup'
$hadVaults = $false
if (Test-Path $appDir) {
    $existingVaults = Get-ChildItem $appDir -File | Where-Object { $_.Name -like '*.iron*' }
    if ($existingVaults) {
        $hadVaults = $true
        Remove-Item -Recurse -Force $vaultBackup -ErrorAction SilentlyContinue
        New-Item -ItemType Directory -Force -Path $vaultBackup | Out-Null
        $existingVaults | ForEach-Object { Copy-Item $_.FullName $vaultBackup }
    }
    Remove-Item -Recurse -Force $appDir
}

if (Test-Path $jpackage) {
    & $jpackage --type app-image --name IronVault --input $inputDir `
        --main-jar IronVault.jar --main-class com.ironvault.IronVaultApp `
        --dest $dist --icon (Join-Path $dist 'IronVault.ico') `
        --app-version 1.0.0 --vendor IronVault `
        --description 'Local password manager'
    if ($LASTEXITCODE -ne 0) { throw 'jpackage failed.' }

    if ($hadVaults) {
        Copy-Item (Join-Path $vaultBackup '*') $appDir -Force
        Remove-Item -Recurse -Force $vaultBackup
        Write-Host 'Existing portable vault preserved.'
    } else {
        if (Test-Path (Join-Path $root 'demo-vault.iron')) {
            Copy-Item (Join-Path $root 'demo-vault.iron') $appDir
        }
        if (Test-Path (Join-Path $root 'demo-vault.iron.key')) {
            Copy-Item (Join-Path $root 'demo-vault.iron.key') $appDir
        }
        Write-Host 'Demo vault copied to the portable app.'
    }
    Write-Host "Portable app: $(Join-Path $appDir 'IronVault.exe')"
} else {
    Write-Host 'jpackage not found; portable app image skipped.'
}

Write-Host 'Build complete.'
