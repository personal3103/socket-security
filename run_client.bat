@echo off
title TCP CLIENT - PTIT
echo ========================================================
echo       CLIENT TCP CHUYEN DOI CHU IN HOA (SSL/TLS)
echo           Sinh vien: Tran Duc Anh - n24dtcn003
echo ========================================================
echo.
echo Dang khoi dong Client...
start "" javaw -cp src vn.edu.ptit.network.securesocket.ClientGUI "Client"
echo [OK] Client da mo thanh cong!
ping 127.0.0.1 -n 2 >nul
exit
