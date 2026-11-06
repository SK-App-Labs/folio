@echo off
set ADB="%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"

echo Disabling Demo Mode on Emulator (emulator-5554)...
%ADB% -s emulator-5554 shell am broadcast -a com.android.systemui.demo -e command exit
echo Demo Mode Disabled. Normal status bar restored.
pause
