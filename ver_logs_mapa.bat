@echo off
echo ========================================
echo Ver logs de MapaActivity (mapamatematicas)
echo ========================================
echo.
echo Presiona Ctrl+C para detener
echo.
adb logcat -c
adb logcat -s MapaActivity:D ProgressLockManager:D






