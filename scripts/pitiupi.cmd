@echo off
setlocal EnableExtensions
cd /d "%~dp0.."
if /I "%~1"=="run" goto run
echo Uso: scripts\pitiupi.cmd run --all
exit /b 1

:run
where ant >nul 2>&1
if errorlevel 1 (
  echo ERRO: ant nao esta no PATH. Instale o Apache Ant e o JDK 17.
  exit /b 1
)
where javac >nul 2>&1
if errorlevel 1 (
  echo ERRO: javac nao esta no PATH. Instale o JDK 17.
  exit /b 1
)

call ant -f "%CD%\pitiupi\build.xml" jar
if errorlevel 1 exit /b 1

if not exist "%CD%\.pitiupi\plugins" mkdir "%CD%\.pitiupi\plugins"
del /q "%CD%\.pitiupi\plugins\*.jar" 2>nul

set "PITIUPI_REPO=%CD%"
set "PITIUPI_PLUGINS=%CD%\.pitiupi\plugins"
powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "$repo = $env:PITIUPI_REPO; $dest = $env:PITIUPI_PLUGINS; $reg = Get-Content -Raw (Join-Path $repo 'plugins.registry.json') | ConvertFrom-Json; foreach ($p in $reg.plugins) { $dir = Join-Path $repo ($p.dir -replace '/','\'); if ($p.build -ne 'ant') { Write-Error ('build nao suportado neste prompt: ' + $p.build); exit 1 }; & ant -f (Join-Path $dir 'build.xml') jar; if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }; $src = Join-Path $dir ($p.jar -replace '/','\'); if (-not (Test-Path -LiteralPath $src)) { Write-Error ('JAR ausente: ' + $src); exit 1 }; Copy-Item -LiteralPath $src -Destination (Join-Path $dest ($p.id + '.jar')) -Force }"
if errorlevel 1 exit /b 1

powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0lib\launch-windows.ps1" -PitiupiHome "%CD%\.pitiupi" -Jar "%CD%\pitiupi\dist\pitiupi.jar"
if errorlevel 1 exit /b 1
echo Janela aberta no Windows. Cabo e Wi-Fi sao as placas desta maquina.
echo Plugins em %CD%\.pitiupi\plugins
exit /b 0
