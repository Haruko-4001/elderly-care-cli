@echo off
chcp 65001 >nul
cd /d "%~dp0"

rd /s /q out 2>nul
echo ===== 编译中 =====
javac -encoding UTF-8 ^
  -cp lib\jackson-core-2.15.4.jar;lib\jackson-databind-2.15.4.jar;lib\jackson-annotations-2.15.4.jar ^
  -d out ^
  src\com\neusoft\yiyang\*.java ^
  src\com\neusoft\yiyang\model\*.java ^
  src\com\neusoft\yiyang\dao\*.java ^
  src\com\neusoft\yiyang\service\*.java ^
  src\com\neusoft\yiyang\cli\*.java

if %errorlevel% equ 0 (
    echo ===== 编译成功 =====
) else (
    echo ===== 编译失败 =====
)
pause
