@echo off
chcp 65001 > nul
echo ==========================================================
echo  🤖 BEAUTY SALON SAAS - SIMULAÇÃO DE USUÁRIO REAL (E2E)  
echo ==========================================================

set TARGET_URL=http://localhost:8081
echo Target URL: %TARGET_URL%

echo.
echo Verificando se a aplicação Spring Boot está ativa na porta 8081...
curl.exe -s -o NUL -w "%%{http_code}" %TARGET_URL%/login | findstr "200 302" > nul
if errorlevel 1 (
    echo ⚠️ ATENÇÃO: A aplicação Spring Boot não respondeu em %TARGET_URL%
    echo Inicie o Spring Boot antes de rodar os testes:
    echo   mvnw.cmd spring-boot:run
    pause
    exit /b 1
)

echo ✅ Aplicação Spring Boot detectada e ativa!
echo 🚀 Iniciando simulação visual no Google Chrome (Headed Mode)...
echo.

cd /d "%~dp0..\e2e"
call npx.cmd playwright test --headed
pause
