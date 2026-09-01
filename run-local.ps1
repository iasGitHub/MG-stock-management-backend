$envFile = Join-Path $PSScriptRoot '.env.local'
if (Test-Path $envFile) {
  Get-Content $envFile | ForEach-Object {
    if ($_ -and $_ -notmatch '^\s*#' -and $_ -match '=') {
      $k, $v = $_ -split '=', 2
      [System.Environment]::SetEnvironmentVariable($k.Trim(), $v.Trim(), 'Process')
    }
  }
} else {
  Write-Warning ".env.local introuvable - utilisation des valeurs par defaut (connexion DB / JWT non configures)"
}

& "$PSScriptRoot\mvnw.cmd" spring-boot:run