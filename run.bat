@echo off
chcp 65001 >nul
cd /d "%~dp0"

java -cp out;lib\jackson-core-2.15.4.jar;lib\jackson-databind-2.15.4.jar;lib\jackson-annotations-2.15.4.jar ^
  com.neusoft.yiyang.Main

pause
