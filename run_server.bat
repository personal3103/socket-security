@echo off
title TCP SERVER - PTIT
echo ========================================================
echo       HE THONG MAY CHU TCP BAO MAT (SSL/TLS - MULTITHREAD)
echo           Sinh vien: Tran Duc Anh - n24dtcn003
echo ========================================================
echo.
echo Dang khoi dong Server Dashboard...
start "" javaw -cp src vn.edu.ptit.network.securesocket.ServerGUI
echo [OK] Server da mo thanh cong!
ping 127.0.0.1 -n 2 >nul
exit
