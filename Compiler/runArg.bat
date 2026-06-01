@echo off
call bats\clean_project.bat

call bats\build_parser.bat
if %ERRORLEVEL% == 1 (goto :parserError)

call bats\build_compiler.bat
if %ERRORLEVEL% == 1 (goto :compilerError)

call bats\test_project.bat %*.8bit
if %ERRORLEVEL% == 1 (goto :testprojectError)

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