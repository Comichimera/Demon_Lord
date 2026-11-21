$path = Join-Path $PSScriptRoot "src\data\version.txt"
$lines = Get-Content $path
$lines = $lines | ForEach-Object {
    if ($_ -match '^build=(\d+)$') {
        'build=' + ([int]$matches[1] + 1)
    } else {
        $_
    }
}
Set-Content $path $lines
