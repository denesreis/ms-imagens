# scripts/start-dev.ps1
# Inicia o ambiente de desenvolvimento no Windows (banco via Docker + app via Maven)

param(
    [switch]$SkipDocker
)

$ProjectDir = Split-Path $PSScriptRoot -Parent
Set-Location $ProjectDir

Write-Host "Iniciando ambiente de desenvolvimento..." -ForegroundColor Cyan

if (-not $SkipDocker) {
    # Subir banco de dados via Docker
    Write-Host "Subindo PostgreSQL..." -ForegroundColor Yellow
    docker-compose -f docker-compose.dev.yml up -d postgres

    # Aguardar banco ficar pronto
    Write-Host "Aguardando PostgreSQL..." -ForegroundColor Yellow
    $maxAttempts = 30
    $attempt = 0
    do {
        $attempt++
        Start-Sleep -Seconds 2
        $ready = docker exec ms-bluedot-postgres-dev pg_isready -U postgres 2>$null
    } while ($LASTEXITCODE -ne 0 -and $attempt -lt $maxAttempts)

    if ($LASTEXITCODE -eq 0) {
        Write-Host "PostgreSQL pronto!" -ForegroundColor Green
    } else {
        Write-Error "PostgreSQL nao iniciou em tempo habil"
        exit 1
    }
}

# Carregar variáveis de ambiente (se existir .env)
if (Test-Path "$ProjectDir\.env") {
    Get-Content "$ProjectDir\.env" | ForEach-Object {
        if ($_ -match '^([^#=]+)=(.*)$') {
            [System.Environment]::SetEnvironmentVariable($matches[1].Trim(), $matches[2].Trim(), "Process")
        }
    }
    Write-Host "Variaveis de ambiente carregadas de .env" -ForegroundColor Green
}

# Detectar Java
$javaExe = "java"
if (Test-Path "C:\Program Files\Java\jdk-21*\bin\java.exe") {
    $javaExe = (Get-ChildItem "C:\Program Files\Java\jdk-21*\bin\java.exe" | Select-Object -First 1).FullName
} elseif ($env:JAVA_HOME) {
    $javaExe = Join-Path $env:JAVA_HOME "bin\java.exe"
}

Write-Host "Iniciando aplicacao (perfil: dev)..." -ForegroundColor Cyan
Write-Host "Swagger UI: http://localhost:8080/api/v1/swagger-ui.html" -ForegroundColor Cyan

# Iniciar via Maven Wrapper
$wrapperJar = ".mvn\wrapper\maven-wrapper.jar"
& $javaExe -classpath $wrapperJar "-Dmaven.multiModuleProjectDirectory=$ProjectDir" `
    org.apache.maven.wrapper.MavenWrapperMain `
    spring-boot:run "-Dspring-boot.run.profiles=dev"
