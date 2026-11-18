@echo off
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "PATH=%JAVA_HOME%\bin;%PATH%"
cd /d "%~dp0"

python scripts\setup_torch.py
if errorlevel 1 pause & exit /b 1

python -m pip install -r requirements.txt
python -m uvicorn main:app --host 127.0.0.1 --port 8000 --reload

pause