start:
	JMP .main
code_area:
.reverse_number:	POP A
	POP B
	MOV [.reverse_number_0], A
	MOV [.reverse_number_n], B
	MOV [.reverse_number_r], 0
.while_0:
	MOV A, [.reverse_number_n]
	MOV B, 0
	CMP A, B
	JA .while_body_0
	JMP .while_exit_0

.while_body_0:
	MOV A, [.reverse_number_r]
	MUL 10
	PUSH A
	PUSH [.reverse_number_n]
	MOV B, 10
	POP A
	MOV C, A
	DIV B
	MUL B
	MOV D, A
	MOV A, C
	SUB A, D
	MOV B, A
	POP A
	ADD A, B
	MOV [.reverse_number_r], A
	MOV A, [.reverse_number_n]
	DIV 10
	PUSH A
	POP A
	MOV [.reverse_number_n], A
	JMP .while_0

.while_exit_0:
	MOV A, [.reverse_number_r]
	MOV B, [.reverse_number_0]
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
	PUSH 251
	CALL .reverse_number
	CALL .print_number

HLT

data_area:
.D: DB 232
.reverse_number_r: DB 0
.reverse_number_0: DB 0
.reverse_number_n: DB 0
.main_0C: DB "reverse(251)="
DB 0
