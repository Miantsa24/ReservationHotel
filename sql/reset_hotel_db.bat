@echo off
echo Resetting MySQL database 'hotel_db'...
set MYSQL_USER=root
set MYSQL_PASS=root
mysql -u %MYSQL_USER% -p%MYSQL_PASS% -e "DROP DATABASE IF EXISTS hotel_db; CREATE DATABASE hotel_db;"
if %errorlevel% equ 0 (
    echo Database 'hotel_db' has been reset successfully.
) else (
    echo Error resetting the database. Please check your credentials and MySQL installation.
)
pause