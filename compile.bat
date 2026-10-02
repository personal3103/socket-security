@echo off
echo ==============================================
echo   BIEN DICH DU AN TCP SECURE SOCKET (PTIT)
echo ==============================================

echo Dang bien dich cac file Java...
javac -encoding UTF-8 src\vn\edu\ptit\network\securesocket\*.java

if %ERRORLEVEL% EQU 0 (
    echo [THANH CONG] Da bien dich xong toan bo file Java!
    copy /Y server.keystore src\ >nul 2>&1
    copy /Y client.truststore src\ >nul 2>&1
    copy /Y server.crt src\ >nul 2>&1
    copy /Y server.keystore src\vn\edu\ptit\network\securesocket\ >nul 2>&1
    copy /Y client.truststore src\vn\edu\ptit\network\securesocket\ >nul 2>&1
    copy /Y server.crt src\vn\edu\ptit\network\securesocket\ >nul 2>&1
) else (
    echo [LOI] Qua trinh bien dich that bai!
)
pause
