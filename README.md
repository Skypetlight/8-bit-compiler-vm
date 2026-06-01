# 8-Bit Compiler + Virtual Machine

A custom compiler and virtual machine ecosystem built around the **8-Bit programming language**, using **ANTLR4**, **Java**, and a stack-based virtual machine architecture.

This project focuses on compiler construction concepts such as:

- Lexical analysis
- Parsing
- Semantic processing
- Assembly generation
- Virtual machine execution
- Function calls and stack handling
- Control flow compilation

The system compiles source code written in the custom 8-Bit language into assembly-like instructions that are later executed by a virtual machine.

---

# Important Credits & Authorship

This repository combines work from multiple authors and academic sources.

## Original Virtual Machine

The original virtual machine implementation was created by:

- Marco Schweighauser (2015)

The original implementation was written in JavaScript.

This project includes a full migration of the virtual machine to Java.

## 8-Bit Language & Grammar

The 8-Bit language design and grammar specification were originally created as part of a university compiler course by the course professor.

## Compiler Implementation

The compiler implementation itself — including parsing integration, code generation, assembly generation, compiler structure, project architecture, and Java implementation — was developed by:

- Alejandro Vega

---

# Technologies Used

- Java
- ANTLR4
- Stack-based Virtual Machine
- Custom Assembly Language
- Batch Scripts

---

# Project Structure

```text
8-bit (Library API)
│
├── Compiler/          # 8-Bit compiler implementation
└── Virtual Machine/   # Java virtual machine implementation
```

---

# Features

## Compiler

The compiler supports:

- Variable declarations
- Arithmetic operations
- Boolean logic
- Comparison operators
- Function declarations
- Function calls
- Return statements
- If/Else statements
- While loops
- For loops
- Recursive functions

## Virtual Machine

The virtual machine executes generated assembly instructions using a stack-based execution model.

Features include:

- Stack operations
- Function call handling
- Program flow control
- Arithmetic execution
- Conditional branching
- Memory handling

---

# Supported Language Constructs

Example constructs supported by the language:

```text
fun factorial(n)
{
    if(n == 0)
        return 1;
    else
        return n * factorial(n - 1);
}
```

---

# Example Test Programs

The repository includes multiple test programs such as:

- Factorial
- Fibonacci
- Gauss summation
- Collatz sequence
- Recursive calls
- Boolean logic tests
- Function return tests

---

# Compiler Architecture

The compiler pipeline follows traditional compiler design stages:

```text
Source Code
    ↓
Lexer (ANTLR4)
    ↓
Parser
    ↓
Parse Tree
    ↓
Semantic Processing
    ↓
Assembly Generation
    ↓
8-Bit Assembly Output
```

---

# Virtual Machine Architecture

The virtual machine executes generated assembly instructions using a stack-oriented runtime model.

Execution includes:

- Instruction decoding
- Stack manipulation
- Memory access
- Function frame handling
- Arithmetic evaluation
- Conditional jumps

---

# Installation

## Requirements

- Java JDK 8+
- ANTLR4
- Windows (batch scripts included)

---

# Compiler Setup

## 1. Navigate to Compiler

```bash
cd Compiler
```

## 2. Build the Parser

```bash
build_parser.bat
```

## 3. Build the Compiler

```bash
build_compiler.bat
```

---

# Running the Compiler

Example:

```bash
runArg.bat factorial.8bit
```

Generated assembly files will be placed inside:

```text
output/
```

---

# Running the Virtual Machine

## 1. Navigate to Virtual Machine

```bash
cd "Virtual Machine"
```

## 2. Build the Virtual Machine

```bash
build.bat
```

## 3. Run an Assembly Program

```bash
run.bat factorial.8bit.asm
```

---

# Grammar

The language grammar is defined using ANTLR4 inside:

```text
Compiler/grammar/EightBit.g4
```

The grammar defines:

- Functions
- Expressions
- Statements
- Operators
- Loops
- Control flow
- Literals
- Function arguments

---

# Sample Operators

## Arithmetic

```text
+
-
*
/
%
```

## Logical

```text
&&
||
!
```

## Relational

```text
==
!=
<
>
<=
>=
```

---

# Educational Goals

This project was developed as part of compiler theory and language processing studies.

It reinforces concepts such as:

- Compiler design
- Parsing theory
- Formal grammars
- Abstract syntax structures
- Stack machines
- Code generation
- Runtime execution models
- Recursive execution

---

# Future Improvements

Possible future improvements include:

- Better error reporting
- Type checking
- Optimizations
- Bytecode generation
- Improved debugging tools
- Interactive REPL
- Cross-platform scripts

---

# License

This repository is intended for educational and portfolio purposes.

Please note that portions of the project are based on academic material and adapted work credited above.
