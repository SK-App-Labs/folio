@echo off
set ADB="%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"

echo Enabling Android System UI Demo Mode on Emulator (emulator-5554)...
%ADB% -s emulator-5554 shell settings put global sysui_demo_allowed 1
%ADB% -s emulator-5554 shell am broadcast -a com.android.systemui.demo -e command enter
%ADB% -s emulator-5554 shell am broadcast -a com.android.systemui.demo -e command clock -e hhmm 0941
%ADB% -s emulator-5554 shell am broadcast -a com.android.systemui.demo -e command battery -e level 100 -e plugged false
%ADB% -s emulator-5554 shell am broadcast -a com.android.systemui.demo -e command network -e wifi show -e level 4
%ADB% -s emulator-5554 shell am broadcast -a com.android.systemui.demo -e command network -e mobile show -e datatype lte -e level 4
%ADB% -s emulator-5554 shell am broadcast -a com.android.systemui.demo -e command notifications -e visible false

echo Demo Mode Enabled: Time is 9:41, Battery 100%%, Wi-Fi Full, Notifications Hidden.
pause
