# Book Management - one-click development launcher
# Double-click start.bat (which calls this script) to:
#   1. check the local database (import db/schema.sql if it is missing,
#      and apply the login-table upgrade db/upgrade_auth.sql when needed)
#   2. start the Spring Boot backend on :8080
#   3. start the Vite dev server on :5173 and open http://localhost:5173/login
#      in an Edge InPrivate window (normal windows on this machine have a
#      system-hook issue that restores the window right after minimizing)

$ErrorActionPreference = 'Stop'

$root        = Split-Path -Parent $MyInvocation.MyCommand.Path
$frontendDir = Join-Path $root 'frontend'
$backendDir  = Join-Path $root 'backend'
$appYml      = Join-Path $backendDir 'src\main\resources\application.yml'
$schemaSql   = Join-Path $root 'db\schema.sql'
$authSql     = Join-Path $root 'db\upgrade_auth.sql'
$frontUrl    = 'http://localhost:5173/login'
$dbName      = 'book_management'

function Write-Step([string]$Message) { Write-Host "==> $Message" -ForegroundColor Cyan }
function Write-Warn([string]$Message) { Write-Host "[WARN] $Message" -ForegroundColor Yellow }
function Write-Fail([string]$Message) { Write-Host "[ERROR] $Message" -ForegroundColor Red }

function Test-Port([int]$Port) {
    # 1) IPv4 loopback (Spring Boot binds both stacks)
    $client = New-Object System.Net.Sockets.TcpClient
    try {
        $handle = $client.BeginConnect('127.0.0.1', $Port, $null, $null)
        if ($handle.AsyncWaitHandle.WaitOne(800)) {
            $client.EndConnect($handle)
            return $true
        }
    } catch {
        # continue with IPv6
    } finally {
        $client.Close()
    }

    # 2) IPv6 loopback: Vite on Windows may bind ::1 only,
    #    and the default TcpClient in .NET Framework is IPv4-only.
    $client6 = $null
    try {
        $client6 = New-Object System.Net.Sockets.TcpClient([System.Net.Sockets.AddressFamily]::InterNetworkV6)
        $handle6 = $client6.BeginConnect([System.Net.IPAddress]::IPv6Loopback, $Port, $null, $null)
        if ($handle6.AsyncWaitHandle.WaitOne(800)) {
            $client6.EndConnect($handle6)
            return $true
        }
    } catch {
        # port is closed
    } finally {
        if ($client6) { $client6.Close() }
    }
    return $false
}

function Wait-Port([int]$Port, [int]$TimeoutSeconds) {
    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        if (Test-Port $Port) { return $true }
        Start-Sleep -Milliseconds 700
    }
    return $false
}

function Find-Mysql {
    $cmd = Get-Command mysql.exe -ErrorAction SilentlyContinue
    if ($cmd) { return $cmd.Source }

    $roots = @('C:\Program Files\MySQL', 'C:\Program Files (x86)\MySQL', 'C:\Program Files\MariaDB 10.11', 'C:\Program Files\MariaDB 11.4')
    foreach ($dir in $roots) {
        if (-not (Test-Path $dir)) { continue }
        $found = Get-ChildItem -Path $dir -Filter 'mysql.exe' -Recurse -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($found) { return $found.FullName }
    }
    return $null
}

function Find-Maven {
    $cmd = Get-Command mvn.cmd -ErrorAction SilentlyContinue
    if ($cmd) { return $cmd.Source }

    if ($env:MAVEN_HOME) {
        $candidate = Join-Path $env:MAVEN_HOME 'bin\mvn.cmd'
        if (Test-Path $candidate) { return $candidate }
    }

    # IntelliJ IDEA install location from the registry
    $regKeys = @(
        'HKLM:\Software\Microsoft\Windows\CurrentVersion\Uninstall\*',
        'HKLM:\Software\WOW6432Node\Microsoft\Windows\CurrentVersion\Uninstall\*',
        'HKCU:\Software\Microsoft\Windows\CurrentVersion\Uninstall\*'
    )
    try {
        $idea = Get-ItemProperty $regKeys -ErrorAction SilentlyContinue |
            Where-Object { $_.DisplayName -match 'IntelliJ IDEA' } |
            Select-Object -First 1
        if ($idea -and $idea.InstallLocation) {
            $candidate = Join-Path $idea.InstallLocation 'plugins\maven\lib\maven3\bin\mvn.cmd'
            if (Test-Path $candidate) { return $candidate }
        }
    } catch { }

    # Common IDE / Maven folders
    $roots = @(
        "$env:LOCALAPPDATA\Programs\IntelliJ IDEA Community Edition",
        'C:\Program Files\JetBrains',
        'D:\programming\idea',
        "$env:LOCALAPPDATA\JetBrains\Toolbox\apps"
    )
    foreach ($dir in $roots) {
        if (-not (Test-Path $dir)) { continue }
        $found = Get-ChildItem -Path $dir -Filter 'mvn.cmd' -Recurse -Depth 8 -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($found) { return $found.FullName }
    }
    return $null
}

function Get-YmlValue([string]$Key, [string]$DefaultValue = '') {
    if (-not (Test-Path $appYml)) { return $DefaultValue }
    $text = Get-Content $appYml -Raw -Encoding UTF8
    $match = [regex]::Match($text, "(?m)^\s*$Key\s*:\s*(.*)$")
    if ($match.Success) {
        return $match.Groups[1].Value.Trim().Trim('"').Trim("'")
    }
    return $DefaultValue
}

function Get-DbState([string]$MysqlExe, [string]$User, [string]$Password) {
    $oldPassword = $env:MYSQL_PWD
    $oldErrorAction = $ErrorActionPreference
    $env:MYSQL_PWD = $Password
    # mysql writes access errors to stderr; keep them as data instead of terminating errors
    $ErrorActionPreference = 'Continue'
    try {
        $output = & $MysqlExe "-u$User" '--connect-timeout=3' '-N' '-e' "SELECT COUNT(*) FROM $dbName.t_book;" 2>&1
        $text = ($output | Out-String)
        if ($LASTEXITCODE -eq 0) { return 'ok' }
        if ($text -match '1045|Access denied') { return 'denied' }
        if ($text -match '1049|1146|Unknown database|n''t exist') { return 'missing' }
        return 'error'
    } finally {
        $ErrorActionPreference = $oldErrorAction
        if ($null -eq $oldPassword) {
            Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
        } else {
            $env:MYSQL_PWD = $oldPassword
        }
    }
}

function Import-Sql([string]$SqlPath, [string]$MysqlExe, [string]$User, [string]$Password) {
    $oldPassword = $env:MYSQL_PWD
    $oldErrorAction = $ErrorActionPreference
    $env:MYSQL_PWD = $Password
    $ErrorActionPreference = 'Continue'
    try {
        Write-Step "Importing $SqlPath ..."
        $commandLine = '"' + $MysqlExe + '" "-u' + $User + '" --default-character-set=utf8mb4 < "' + $SqlPath + '"'
        cmd.exe /c $commandLine
        return ($LASTEXITCODE -eq 0)
    } finally {
        $ErrorActionPreference = $oldErrorAction
        if ($null -eq $oldPassword) {
            Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
        } else {
            $env:MYSQL_PWD = $oldPassword
        }
    }
}

function Get-TableState([string]$MysqlExe, [string]$User, [string]$Password, [string]$Table) {
    $oldPassword = $env:MYSQL_PWD
    $oldErrorAction = $ErrorActionPreference
    $env:MYSQL_PWD = $Password
    $ErrorActionPreference = 'Continue'
    try {
        $output = & $MysqlExe "-u$User" '--connect-timeout=3' '-N' '-e' "SELECT COUNT(*) FROM $dbName.$Table;" 2>&1
        $text = ($output | Out-String)
        if ($LASTEXITCODE -eq 0) { return 'ok' }
        if ($text -match '1146|doesn''t exist|n''t exist') { return 'missing' }
        return 'error'
    } finally {
        $ErrorActionPreference = $oldErrorAction
        if ($null -eq $oldPassword) {
            Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
        } else {
            $env:MYSQL_PWD = $oldPassword
        }
    }
}

# ------------------------------------------------------------------
# 0. Prerequisites
# ------------------------------------------------------------------
Write-Step 'Checking Node.js / Java ...'
$node = Get-Command node -ErrorAction SilentlyContinue
$npm  = Get-Command npm.cmd -ErrorAction SilentlyContinue
$java = Get-Command java -ErrorAction SilentlyContinue
if (-not $node -or -not $npm) {
    Write-Fail 'Node.js (node/npm) was not found in PATH. Install Node.js 20+ first.'
    exit 1
}
if (-not $java) {
    Write-Fail 'Java was not found in PATH. Install JDK 21 first.'
    exit 1
}

# ------------------------------------------------------------------
# 1. Database
# ------------------------------------------------------------------
$mysql = Find-Mysql
if ($mysql) {
    $dbUser = Get-YmlValue 'username' 'root'
    $dbPass = Get-YmlValue 'password' '123456'
    Write-Step "Checking database '$dbName' (user: $dbUser) ..."
    $state = Get-DbState $mysql $dbUser $dbPass

    if ($state -eq 'missing') {
        Write-Warn "Database '$dbName' was not found. Importing db/schema.sql with the configured account ..."
        if (Import-Sql $schemaSql $mysql $dbUser $dbPass) {
            $state = Get-DbState $mysql $dbUser $dbPass
        }
    }

    if ($state -ne 'ok') {
        Write-Warn "Database check failed ($state). You can enter the MySQL account and initialize it now."
        $answer = Read-Host 'Run database setup now? (y/N)'
        if ($answer -match '^[yY]') {
            & (Join-Path $root 'setup-db.ps1')
            $dbUser = Get-YmlValue 'username' 'root'
            $dbPass = Get-YmlValue 'password' '123456'
            $state = Get-DbState $mysql $dbUser $dbPass
        }
    }

    if ($state -ne 'ok') {
        Write-Fail 'Cannot connect to MySQL with the account in backend/src/main/resources/application.yml.'
        Write-Host '        Double-click setup-db.bat once, enter the correct MySQL password, then run start.bat again.' -ForegroundColor Yellow
        exit 1
    }

    # Login table t_user: apply the incremental script to existing databases
    # without touching the book / category data.
    $userTable = Get-TableState $mysql $dbUser $dbPass 't_user'
    if ($userTable -ne 'ok') {
        Write-Step 'Applying login/permission table upgrade (db/upgrade_auth.sql) ...'
        if (Import-Sql $authSql $mysql $dbUser $dbPass) {
            $userTable = Get-TableState $mysql $dbUser $dbPass 't_user'
        }
    }
    if ($userTable -ne 'ok') {
        Write-Warn 'Table t_user is still missing. Login will not work until db/upgrade_auth.sql is applied manually.'
    }
    Write-Step 'Database is ready (including login tables).'
} else {
    Write-Warn 'mysql.exe was not found. Skip database pre-check; backend will connect by itself.'
}

# ------------------------------------------------------------------
# 2. Frontend dependencies
# ------------------------------------------------------------------
if (-not (Test-Path (Join-Path $frontendDir 'node_modules'))) {
    Write-Step 'Installing frontend dependencies (npm install), first run only ...'
    Push-Location $frontendDir
    try {
        & $npm.Source install
        if ($LASTEXITCODE -ne 0) {
            Write-Fail 'npm install failed. Check the network or npm registry and try again.'
            exit 1
        }
    } finally {
        Pop-Location
    }
}

# ------------------------------------------------------------------
# 3. Backend
# ------------------------------------------------------------------
if (Test-Port 8080) {
    Write-Step 'Backend is already running on :8080, skip.'
} else {
    $jar = Get-ChildItem -Path (Join-Path $backendDir 'target') -Filter 'book-backend-*.jar' -File -ErrorAction SilentlyContinue |
        Select-Object -First 1

    # The jar embeds application.yml, so rebuild when backend sources/config changed
    $needsBuild = -not $jar
    if ($jar) {
        $newestSource = Get-ChildItem -Path (Join-Path $backendDir 'src') -Recurse -File -ErrorAction SilentlyContinue |
            Sort-Object LastWriteTime -Descending | Select-Object -First 1
        if ($newestSource -and $jar.LastWriteTime -lt $newestSource.LastWriteTime) {
            Write-Step 'Backend sources are newer than the jar, rebuilding ...'
            $needsBuild = $true
        }
    }

    if ($needsBuild) {
        $maven = Find-Maven
        if (-not $maven) {
            Write-Fail 'Maven was not found. Open the backend project in IntelliJ IDEA, or install Maven first.'
            exit 1
        }
        Write-Step "Building backend jar with Maven (may take a few minutes) ..."
        Push-Location $backendDir
        try {
            & $maven '-DskipTests' 'package'
            if ($LASTEXITCODE -ne 0) {
                Write-Fail 'Backend build failed. See the Maven output above.'
                exit 1
            }
        } finally {
            Pop-Location
        }
        $jar = Get-ChildItem -Path (Join-Path $backendDir 'target') -Filter 'book-backend-*.jar' -File -ErrorAction SilentlyContinue |
            Select-Object -First 1
    }

    if (-not $jar) {
        Write-Fail 'Backend jar not found after the build. Check backend/pom.xml.'
        exit 1
    }

    Write-Step 'Starting backend on :8080 ...'
    Start-Process -FilePath $java.Source -ArgumentList ('-jar "' + $jar.FullName + '"') -WorkingDirectory $backendDir | Out-Null

    if (-not (Wait-Port 8080 120)) {
        Write-Fail 'Backend did not start within 120 seconds. Check the Spring Boot console window for errors.'
        exit 1
    }
    Write-Step 'Backend is up.'
}

# ------------------------------------------------------------------
# 4. Frontend + browser
# ------------------------------------------------------------------
function Find-Edge {
    $cmd = Get-Command msedge.exe -ErrorAction SilentlyContinue
    if ($cmd) { return $cmd.Source }

    $regKeys = @(
        'HKLM:\SOFTWARE\Microsoft\Windows\CurrentVersion\App Paths\msedge.exe',
        'HKLM:\SOFTWARE\WOW6432Node\Microsoft\Windows\CurrentVersion\App Paths\msedge.exe',
        'HKCU:\SOFTWARE\Microsoft\Windows\CurrentVersion\App Paths\msedge.exe'
    )
    foreach ($key in $regKeys) {
        try {
            $path = (Get-ItemProperty -Path $key -ErrorAction Stop).'(Default)'
            if ($path -and (Test-Path $path)) { return $path }
        } catch { }
    }

    $candidates = @(
        "$env:ProgramFiles\Microsoft Edge\Application\msedge.exe",
        "${env:ProgramFiles(x86)}\Microsoft Edge\Application\msedge.exe",
        "$env:LOCALAPPDATA\Microsoft\Edge\Application\msedge.exe"
    )
    foreach ($path in $candidates) {
        if (Test-Path $path) { return $path }
    }
    return $null
}

function Open-AppWindow([string]$Url) {
    $edge = Find-Edge
    if ($edge) {
        # Plan B: open the app in an InPrivate window.
        # On this machine a normal Edge window is restored by a system hook
        # right after clicking minimize; InPrivate windows are not affected.
        Write-Step 'Opening the app in an Edge InPrivate window ...'
        Start-Process -FilePath $edge -ArgumentList @('--inprivate', $Url) | Out-Null
        return
    }

    Write-Warn 'Microsoft Edge was not found. Opening the page with the default browser (normal window).'
    try {
        # Use "cmd start" instead of Start-Process <url>, which is more reliable on Windows
        Start-Process -FilePath 'cmd.exe' -ArgumentList '/c', ('start "" "' + $Url + '"') -WindowStyle Hidden | Out-Null
    } catch {
        Write-Warn "Could not open the browser automatically. Please visit $Url manually."
    }
}

if (Test-Port 5173) {
    Write-Step 'Frontend is already running on :5173, opening the login page ...'
    Open-AppWindow $frontUrl
} else {
    Write-Step 'Starting Vite dev server on :5173 ...'
    Start-Process -FilePath 'cmd.exe' -ArgumentList '/c "npm run dev"' -WorkingDirectory $frontendDir | Out-Null
    if (-not (Wait-Port 5173 60)) {
        Write-Fail 'Frontend did not start within 60 seconds. Check the Vite console window for errors.'
        exit 1
    }
    Open-AppWindow $frontUrl
}

Write-Host ''
Write-Host 'Launch finished.' -ForegroundColor Green
Write-Host "  UI : $frontUrl"
Write-Host '  API: http://localhost:8080/api'
Write-Host '  Accounts: admin / admin123 (ADMIN), reader / user123 (USER)'
Write-Host 'Close the backend / Vite console windows to stop the services.'
exit 0
