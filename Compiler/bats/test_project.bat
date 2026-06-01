@echo off
REM Path to ANTLR jar
set ANTLR_JAR=ANTLR4\antlr-4.13.2-complete.jar

echo Prueba una caso de prueba: %1
java -cp .;lib;%ANTLR_JAR% eightBit.compiler.EightBitc cases\%1 > output\%1.asm