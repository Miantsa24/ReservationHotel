@echo off
echo Resetting MySQL database 'hotel_db'...
set MYSQL_USER=root
set MYSQL_PASS=root
set MYSQL_PATH="C:\xampp\mysql\bin\mysql.exe"
if not exist %MYSQL_PATH% (
    echo MySQL not found at %MYSQL_PATH%. Please install MySQL or update the path in this script.
    pause
    exit /b 1
)
%MYSQL_PATH% -u %MYSQL_USER% -p%MYSQL_PASS% -e "DROP DATABASE IF EXISTS hotel_db; CREATE DATABASE hotel_db;"
if %errorlevel% equ 0 (
    echo Database 'hotel_db' has been reset successfully.
    echo Executing init_database.sql...
    %MYSQL_PATH% -u %MYSQL_USER% -p%MYSQL_PASS% hotel_db < "%~dp0init_database.sql"
    if %errorlevel% equ 0 (
        echo init_database.sql executed successfully.
        echo Executing data.sql...
        %MYSQL_PATH% -u %MYSQL_USER% -p%MYSQL_PASS% hotel_db < "%~dp0data.sql"
        if %errorlevel% equ 0 (
            echo data.sql executed successfully.
        ) else (
            echo Error executing data.sql.
        )
    ) else (
        echo Error executing init_database.sql.
    )
) else (
    echo Error resetting the database. Please check your credentials and MySQL installation.
)
pause