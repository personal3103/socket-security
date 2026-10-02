@echo off
title CHAY NHIEU CLIENT DONG THOI - PTIT
echo ========================================================
echo       MO N CLIENT DONG THOI (MULTITHREAD TEST)
echo           Sinh vien: Tran Duc Anh - n24dtcn003
echo ========================================================
echo.
set /p NUM="Nhap so luong client muon mo (Mac dinh la 3): "
if "%NUM%"=="" set NUM=3
echo Dang khoi chay %NUM% client dong thoi...
for /L %%i in (1, 1, %NUM%) do (
    start "" javaw -cp src vn.edu.ptit.network.securesocket.ClientGUI "Client %%i"
    ping 127.0.0.1 -n 2 >nul
)
echo [OK] Da mo xong %NUM% client!
ping 127.0.0.1 -n 2 >nul
exit
