# Book Management - one-time database setup
# Run by double-clicking setup-db.bat.
# It asks for the local MySQL account, imports db/schema.sql,
# and writes the same account into backend/src/main/resources/application.yml.

$ErrorActionPreference = 'Stop'

$root      = Split-Path -Parent $MyInvocation.MyCommand.Path
$appYml    = Join-Path $root 'backend\src\main\resources\application.yml'
$schemaSql = Join-Path $root 'db\schema.sql'

function Write-Step([string]$Message) { Write-Host "==> $Message" -ForegroundColor Cyan }
function Write-Fail([string]$Message) { Write-Host "[ERROR] $Message" -ForegroundColor Red }

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

function ConvertTo-YamlSingleQuoted([string]$Value) {
    return "'" + ($Value -replace "'", "''") + "'"
}

$mysql = Find-Mysql
if (-not $mysql) {
    Write-Fail 'mysql.exe was not found. Install MySQL 8.0 or add its bin folder to PATH.'
    exit 1
}

$dbUser = Read-Host 'MySQL user (press Enter to use root)'
if ([string]::IsNullOrWhiteSpace($dbUser)) { $dbUser = 'root' }

$secure = Read-Host "MySQL password for user '$dbUser'" -AsSecureString
$bstr = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
try {
    $dbPass = [System.Runtime.InteropServices.Marshal]::PtrToStringBSTR($bstr)
} finally {
    [System.Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr)
}

$oldPassword = $env:MYSQL_PWD
$oldErrorAction = $ErrorActionPreference
$env:MYSQL_PWD = $dbPass
# mysql writes errors to stderr; treat them as normal output, not terminating errors
$ErrorActionPreference = 'Continue'
try {
    Write-Step 'Testing MySQL connection ...'
    & $mysql "-u$dbUser" '--connect-timeout=3' '-N' '-e' 'SELECT VERSION();' | Out-Null
    if ($LASTEXITCODE -ne 0) {
        Write-Fail 'Cannot connect to MySQL with the given account. Check the username/password and make sure MySQL80 service is running.'
        exit 1
    }

    Write-Step 'Importing db/schema.sql (creates database, tables and sample data) ...'
    $commandLine = '"' + $mysql + '" "-u' + $dbUser + '" --default-character-set=utf8mb4 < "' + $schemaSql + '"'
    cmd.exe /c $commandLine
    if ($LASTEXITCODE -ne 0) {
        Write-Fail 'Failed to import db/schema.sql. See the message above.'
        exit 1
    }

    Write-Step 'Verifying sample data ...'
    & $mysql "-u$dbUser" '-N' '-e' 'SELECT COUNT(*) FROM book_management.t_category; SELECT COUNT(*) FROM book_management.t_book;' |
        ForEach-Object { Write-Host "    row count: $_" }
} finally {
    $ErrorActionPreference = $oldErrorAction
    if ($null -eq $oldPassword) {
        Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
    } else {
        $env:MYSQL_PWD = $oldPassword
    }
}

Write-Step 'Writing database credentials into application.yml ...'
$text = Get-Content $appYml -Raw -Encoding UTF8
$evaluatorUser = [System.Text.RegularExpressions.MatchEvaluator] {
    param($match)
    return $match.Groups[1].Value + (ConvertTo-YamlSingleQuoted $dbUser)
}
$evaluatorPass = [System.Text.RegularExpressions.MatchEvaluator] {
    param($match)
    return $match.Groups[1].Value + (ConvertTo-YamlSingleQuoted $dbPass)
}
$text = [regex]::Replace($text, '(?m)^(\s*username:\s*).*$', $evaluatorUser)
$text = [regex]::Replace($text, '(?m)^(\s*password:\s*).*$', $evaluatorPass)
[System.IO.File]::WriteAllText($appYml, $text, (New-Object System.Text.UTF8Encoding($false)))

Write-Host ''
Write-Host 'Database setup finished.' -ForegroundColor Green
