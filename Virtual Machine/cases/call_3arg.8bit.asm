start:
	JMP .main
code_area:
.f:	POP A
	POP B
	POP C
	POP D
	MOV [.f_0], A
	MOV [.f_x], B
	MOV [.f_y], C
	MOV [.f_z], D
	MOV A, [.f_x]
	ADD A, [.f_y]
	PUSH A
	MOV B, [.f_z]
	POP A
	ADD A, B
	MOV B, [.f_0]
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
	PUSH 22
	PUSH 22
	PUSH 22
	CALL .f
	CALL .print_number

HLT

data_area:
.D: DB 232
.f_0: DB 0
.f_x: DB 0
.f_y: DB 0
.f_z: DB 0
.main_0C: DB "f(22, 22, 22)="
DB 0
