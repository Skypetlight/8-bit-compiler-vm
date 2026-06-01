@echo off
setlocal enabledelayedexpansion

:: Delete and recreate the bin folder
if exist bin (
    rd /s /q bin
)
mkdir bin

:: Start building javac command
set "files="

for /R src %%f in (*.java) do (
    set "file=%%f"
    set "file=!file:\=\\!"
    set "files=!files! "!file!""
)

:: Compile all files at once
javac -d bin !files!
