start:
	JMP .main
code_area:
.collatz:	POP A
	POP B
	MOV [.collatz_0], A
	MOV [.collatz_n], B
.while_0:
	MOV A, [.collatz_n]
	MOV B, 1
	CMP A, B
	JNZ .while_body_0
	JMP .while_exit_0

.while_body_0:
	PUSH [.collatz_n]
	MOV B, 2
	POP A
	MOV C, A
	DIV B
	MUL B
	MOV D, A
	MOV A, C
	SUB A, D
	PUSH A
	MOV B, 0
	POP A
	CMP A, B
	JE .accept_0
	JMP .reject_0

.accept_0:
	MOV A, [.collatz_n]
	DIV 2
	PUSH A
	POP A
	MOV [.collatz_n], A
	JMP .ifend_0

.reject_0:
	MOV A, 3
	MUL [.collatz_n]
	PUSH A
	MOV B, 1
	POP A
	ADD A, B
	MOV [.collatz_n], A

.ifend_0:
	JMP .while_0

.while_exit_0:
	MOV A, 0
	MOV B, [.collatz_0]
	PUSH A
	PUSH B
	RET

.print_number:
	MOV D, [.D]
	POP C
	POP A
	PUSH 255
	PUSH A
.print_int:
	DIV 10
	POP B
	PUSH A
	MUL 10
	SUB B, A
	POP A
	PUSH B
	CMP A, 0
	JZ .print_int_method
	PUSH A
	JMP .print_int
.print_int_method:
	POP A
	CMP A, 255
	JZ .print_int_exit
	ADD A, 48
	MOV [D], A
	INC D
	JMP .print_int_method
.print_int_exit:
	MOV [.D], D
	PUSH C
	RET

main_area:
.main:
	PUSH 8
	CALL .collatz
	CALL .print_number

HLT

data_area:
.D: DB 232
.collatz_0: DB 0
.collatz_n: DB 0
