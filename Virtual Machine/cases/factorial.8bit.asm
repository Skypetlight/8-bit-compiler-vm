start:
	JMP .main
code_area:
.fact:	POP A
	POP B
	PUSH [.fact_n]
	PUSH [.fact_0]
	MOV [.fact_0], A
	MOV [.fact_n], B
	MOV A, [.fact_n]
	MOV B, 0
	CMP A, B
	JE .accept_0
	JMP .reject_0

.accept_0:
	MOV A, 1
	MOV B, [.fact_0]
	POP C
	MOV [.fact_0], C
	POP C
	MOV [.fact_n], C
	PUSH A
	PUSH B
	RET

.reject_0:
	PUSH [.fact_n]
	MOV A, [.fact_n]
	SUB A, 1
	PUSH A
	CALL .fact
	POP B
	POP A
	MUL B
	MOV B, [.fact_0]
	POP C
	MOV [.fact_0], C
	POP C
	MOV [.fact_n], C
	PUSH A
	PUSH B
	RET

.print_string:
	MOV D, [.D]
	POP A
	POP B
	PUSH A
	MOV A, 0
.print_string_method:
	MOV C, [B]
	MOV [D], C
	INC B
	INC D
	CMP A, [B]
	JNZ .print_string_method
.print_string_exit:
	MOV [.D], D
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
	PUSH .main_0C
	CALL .print_string
	PUSH 5
	CALL .fact
	CALL .print_number

HLT

data_area:
.D: DB 232
.fact_0: DB 0
.fact_n: DB 0
.main_0C: DB "fact(5)="
DB 0
