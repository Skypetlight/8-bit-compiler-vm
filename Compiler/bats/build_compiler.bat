@echo off
REM Path to ANTLR jar
set ANTLR_JAR=ANTLR4\antlr-4.13.2-complete.jar

REM compila modelo y compilador
javac -Xlint:unchecked -cp .;lib;%ANTLR_JAR%  -d lib src/eightBit/asm/*.java src/eightBit/compiler/*.java src/eightBit/registry/*.java 
