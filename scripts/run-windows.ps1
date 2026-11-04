# Chamado por scripts/pitiupi.sh (WSL ou Git Bash) e por scripts/pitiupi.cmd.
# Não copia plugins: quem chama já deixou o JAR e o PITIUPI_HOME prontos.
param(
    [Parameter(Mandatory = $true)][string]$PitiupiHome,
    [Parameter(Mandatory = $true)][string]$Jar
)

$ErrorActionPreference = 'Stop'

function Find-JavaExe {
    $cmd = Get-Command java -ErrorAction SilentlyContinue
    if ($cmd -and $cmd.Source -notmatch '\\WindowsApps\\') {
        return $cmd.Source
    }
    $patterns = @(
        (Join-Path $env:ProgramFiles 'Microsoft\jdk-*\bin\java.exe'),
        (Join-Path $env:ProgramFiles 'Eclipse Adoptium\jdk-*\bin\java.exe'),
        (Join-Path $env:ProgramFiles 'Java\jdk-*\bin\java.exe'),
        (Join-Path ${env:ProgramFiles(x86)} 'Microsoft\jdk-*\bin\java.exe'),
        (Join-Path $env:LOCALAPPDATA 'Programs\Microsoft\jdk-*\bin\java.exe')
    )
    foreach ($pat in $patterns) {
        $hit = Get-Item -Path $pat -ErrorAction SilentlyContinue |
            Sort-Object FullName -Descending |
            Select-Object -First 1
        if ($hit) { return $hit.FullName }
    }
    return $null
}

if (-not (Test-Path -LiteralPath $Jar)) {
    Write-Error "JAR ausente: $Jar"
}
if (-not (Test-Path -LiteralPath $PitiupiHome)) {
    Write-Error "PITIUPI_HOME ausente: $PitiupiHome"
}

$javaExe = Find-JavaExe
if (-not $javaExe) {
    Write-Error "Java do Windows nao encontrado. No PowerShell: winget install Microsoft.OpenJDK.17 --accept-package-agreements"
}

Get-CimInstance Win32_Process -Filter "Name = 'java.exe' OR Name = 'javaw.exe'" -ErrorAction SilentlyContinue |
    Where-Object { $_.CommandLine -and $_.CommandLine -like '*pitiupi.jar*' } |
    ForEach-Object { Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue }

$env:PITIUPI_HOME = $PitiupiHome
Start-Process -FilePath $javaExe -ArgumentList @('-jar', $Jar)
