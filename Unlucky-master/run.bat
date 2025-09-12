@echo off
echo === Build APK ===
call gradlew.bat android:assembleDebug

echo === Install APK to Emulator ===
adb install -r android\build\outputs\apk\debug\android-debug.apk

echo === Done! ===

