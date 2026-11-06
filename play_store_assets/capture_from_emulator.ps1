# File: play_store_assets/capture_from_emulator.ps1
param(
    [Parameter(Mandatory=$false)]
    [string]$Slot = "1"
)

$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"

$slotNames = @{
    "1" = "screenshot_1_daily_emu.png"
    "2" = "screenshot_2_calendar_emu.png"
    "3" = "screenshot_3_cards_emu.png"
    "4" = "screenshot_4_budgets_emu.png"
    "5" = "screenshot_5_accounts_emu.png"
}

$fileName = if ($slotNames.ContainsKey($Slot)) { $slotNames[$Slot] } else { "$Slot.png" }
$outPath = Join-Path $PSScriptRoot $fileName

Write-Host "Capturing emulator screen to $fileName..." -ForegroundColor Cyan
& $adb -s emulator-5554 exec-out screencap -p > $outPath

if (Test-Path $outPath) {
    $size = (Get-Item $outPath).Length / 1KB
    Write-Host "Successfully captured: $fileName ($([math]::Round($size, 1)) KB)" -ForegroundColor Green
} else {
    Write-Host "Failed to capture screenshot." -ForegroundColor Red
}
