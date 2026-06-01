@echo off
echo Running all .8bit.asm files in the "cases" folder...

REM Loop through all .8bit.asm files in the "cases" folder
for %%F in (cases\*.8bit.asm) do (
    echo Running VM with file: %%~nxF
    java -cp bin eightBit.vm.Main "%%F"
    echo --------------------------------------
)

echo All files have been processed.
pause