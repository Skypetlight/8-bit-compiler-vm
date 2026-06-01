@echo off
REM Genera parser y visitors y los compila
REM Asume antlr4 esta en el PATH

REM Path to the ANTLR jar file (update if needed)
set ANTLR_JAR=ANTLR4\antlr-4.13.2-complete.jar

java -cp "%ANTLR_JAR%" org.antlr.v4.Tool -visitor -o src/eightBit/antlr -package eightBit.antlr -no-listener grammar/EightBit.g4 %*
if %ERRORLEVEL% == 1 (goto :error)

REM Compile with ANTLR runtime in classpath
javac -cp "%ANTLR_JAR%" -d lib -Xlint:deprecation src/eightBit/antlr/*.java
goto :end

:error
echo "*** ANTLR compilation failed ***"
:end