@echo off
title MENU DIEU KHIEN TCP BTM SOCKET - PTIT

:MENU
cls
echo ====================================================================
echo       BAI THUC HANH LAP TRINH MANG - TCP BTM SOCKET (MULTITHREAD)
echo            Sinh vien: Tran Duc Anh - MSSV: n24dtcn003 - PTIT
echo ====================================================================
echo.
echo   [1] Khoi chay SERVER (Dashboard Quan tri Server TCP)
echo   [2] Mo them 1 CLIENT moi (Nhap ten tuy y)
echo   [3] Mo N CLIENT cung luc (Test dong thoi da luong Multithread)
echo   [4] Mo KICH BAN CHUAN (Tu dong chia 3 cua so: Server + 2 Client song song)
echo   [5] Chay kiem thu tu dong (TestSecureSocket)
echo   [6] Bien dich lai toan bo ma nguon Java
echo   [0] Thoat
echo.
echo ====================================================================
set /p CHOICE="Vui long chon chuc nang (0-6): "

if "%CHOICE%"=="1" goto RUN_SERVER
if "%CHOICE%"=="2" goto RUN_ONE_CLIENT
if "%CHOICE%"=="3" goto RUN_N_CLIENTS
if "%CHOICE%"=="4" goto RUN_SCENARIO
if "%CHOICE%"=="5" goto RUN_TEST
if "%CHOICE%"=="6" goto RECOMPILE
if "%CHOICE%"=="0" exit
goto MENU

:RUN_SERVER
echo.
echo Dang khoi dong Server Dashboard...
start "" javaw -cp src vn.edu.ptit.network.securesocket.ServerGUI
echo [OK] Server da mo thanh cong!
ping 127.0.0.1 -n 2 >nul
goto MENU

:RUN_ONE_CLIENT
echo.
set /p CNAME="Nhap ten Client muon tao (Vi du: Tran Duc Anh, Client 1...): "
if "%CNAME%"=="" set CNAME=Client_%RANDOM%
echo Dang khoi dong Client: %CNAME%...
start "" javaw -cp src vn.edu.ptit.network.securesocket.ClientGUI "%CNAME%"
ping 127.0.0.1 -n 2 >nul
goto MENU

:RUN_N_CLIENTS
echo.
set /p NUM="Nhap so luong N client muon mo (Vi du: 3): "
if "%NUM%"=="" set NUM=3
echo Dang khoi chay %NUM% client dong thoi...
for /L %%i in (1, 1, %NUM%) do (
    start "" javaw -cp src vn.edu.ptit.network.securesocket.ClientGUI "Client %%i"
    ping 127.0.0.1 -n 2 >nul
)
echo [OK] Da mo xong %NUM% client!
ping 127.0.0.1 -n 2 >nul
goto MENU

:RUN_SCENARIO
echo.
echo Dang khoi chay kich ban mau (Server ben trai, 2 Client ben phai song song)...
start "" javaw -cp src vn.edu.ptit.network.securesocket.Main
echo [OK] Da mo thanh cong 3 cua so song song de tien chup anh bao cao!
ping 127.0.0.1 -n 2 >nul
goto MENU

:RUN_TEST
echo.
echo Dang chay kiem thu tu dong...
java -cp src vn.edu.ptit.network.securesocket.TestSecureSocket
pause
goto MENU

:RECOMPILE
echo.
echo Dang tien hanh bien dich lai toan bo file Java...
javac -encoding UTF-8 src\vn\edu\ptit\network\securesocket\*.java
if errorlevel 1 (
    echo [LOI] Co loi khi bien dich!
) else (
    echo [THANH CONG] Da bien dich toan bo thanh cong!
    copy /Y server.keystore src\ >nul 2>&1
    copy /Y client.truststore src\ >nul 2>&1
    copy /Y server.crt src\ >nul 2>&1
)
pause
goto MENU
