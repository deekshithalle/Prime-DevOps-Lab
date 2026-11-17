param([switch]$TestOnly)
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
New-Item -ItemType Directory -Force out | Out-Null
javac -d out src/PrimeNumber.java src/PrimeNumberTest.java src/PrimeServer.java
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
java -cp out PrimeNumberTest
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
if (-not $TestOnly) { java -cp out PrimeServer }
