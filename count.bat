@echo off
for /r src %%f in (*.java) do (
  for /f "usebackq tokens=*" %%a in ("%%f") do set /a n+=1
  call echo %%~nxf: %%n%% lines
  set n=0
)
