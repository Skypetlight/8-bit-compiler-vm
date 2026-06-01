@echo off
call bats\clean_project.bat

call bats\build_parser.bat
if %ERRORLEVEL% == 1 (goto :parserError)

call bats\build_compiler.bat
if %ERRORLEVEL% == 1 (goto :compilerError)

REM Path to ANTLR jar
set ANTLR_JAR=ANTLR4\antlr-4.13.2-complete.jar

for %%F in (cases\*) do (
    echo Probando caso de prueba: %%~nxF
    java -cp .;lib;%ANTLR_JAR% eightBit.compiler.EightBitc "cases\%%~nxF" > "output\%%~nF.8bit.asm"
)

for %%F in (cases_extra\*) do (
    echo Probando caso de prueba: %%~nxF
    java -cp .;lib;%ANTLR_JAR% eightBit.compiler.EightBitc "cases_extra\%%~nxF" > "output_extra\%%~nF.8bit.asm"
)

goto :end

:parserError
	goto :end

:compilerError
	echo "*** Compiler compilation failed ***"
	goto :end

:testprojectError
	echo "*** Test case failed ***"
	goto :end

:end
	pause