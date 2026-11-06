@echo off
cd /d "%~dp0"

python setup_torch.py
if errorlevel 1 pause & exit /b 1

python -m pip install -r requirements.txt
python -m uvicorn main:app --host 127.0.0.1 --port 8000

pause