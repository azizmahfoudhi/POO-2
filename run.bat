@echo off
echo ========================================================
echo Demarrage du Systeme de Gestion des Clubs de l'IHEC...
echo ========================================================
echo.
echo Veuillez patienter pendant que Spring Boot se connecte a Supabase...
echo.
call mvnw.cmd spring-boot:run
pause
