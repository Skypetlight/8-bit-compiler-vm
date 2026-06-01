start:
	JMP .main
code_area:
.fibo:	POP A
	POP B
	MOV [.fibo_0], A
	MOV [.fibo_n], B
	MOV [.fibo_a], 1
	MOV [.fibo_b], 1
	MOV [.fibo_t], 0
.while_0:
	MOV A, [.fibo_n]
	MOV B, 0
	CMP A, B
	JNZ .while_body_0
	JMP .while_exit_0

.while_body_0:
	MOV A, [.fibo_a]
	MOV [.fibo_t], A
	MOV A, [.fibo_b]
	MOV [.fibo_a], A
	MOV A, [.fibo_b]
	ADD A, [.fibo_t]
	MOV [.fibo_b], A
	MOV A, [.fibo_n]
	SUB A, 1
	MOV [.fibo_n], A
	JMP .while_0

.while_exit_0:
	MOV A, [.fibo_a]
	MOV B, [.fibo_0]
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
	PUSH 3
	CALL .fibo
	CALL .print_number

HLT

data_area:
.D: DB 232
.fibo_a: DB 0
.fibo_b: DB 0
.fibo_t: DB 0
.fibo_0: DB 0
.fibo_n: DB 0
.main_0C: DB "fibo(3)="
DB 0
