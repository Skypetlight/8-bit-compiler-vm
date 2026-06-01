start:
	JMP .main
code_area:
.fact:	POP A
	POP B
	MOV [.fact_0], A
	MOV [.fact_n], B
	MOV [.fact_f], 1
.while_0:
	MOV A, [.fact_n]
	MOV B, 0
	CMP A, B
	JA .while_body_0
	JMP .while_exit_0

.while_body_0:
	MOV A, [.fact_f]
	MUL [.fact_n]
	PUSH A
	POP A
	MOV [.fact_f], A
	MOV A, [.fact_n]
	SUB A, 1
	MOV [.fact_n], A
	JMP .while_0

.while_exit_0:
	MOV A, [.fact_f]
	MOV B, [.fact_0]
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
.fact_f: DB 0
.fact_0: DB 0
.fact_n: DB 0
.main_0C: DB "fact(5)="
DB 0
