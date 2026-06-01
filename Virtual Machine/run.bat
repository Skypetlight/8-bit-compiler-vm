@echo off
REM Check if an argument was provided
if "%1"=="" (
    echo Usage: run [filename without extension]
    goto :end
)

REM Build the full path to the .8bit.asm file using the argument
set FILE=cases\%1.8bit.asm

REM Run the VM with the specified file
java -cp bin eightBit.vm.Main %FILE%

:end