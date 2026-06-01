start:
	JMP .main
code_area:
.add1:	POP A
	POP B
	MOV [.add1_0], A
	MOV [.add1_x], B
	MOV A, [.add1_x]
	MOV B, [.add1_0]
	PUSH A
	PUSH B
	RET

.add2:	POP A
	POP B
	MOV [.add2_0], A
	MOV [.add2_y], B
	MOV A, [.add2_y]
	MOV B, [.add2_0]
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
	PUSH 10
	CALL .add1
	PUSH 56
	CALL .add2
	POP B
	POP A
	ADD A, B
	PUSH A
	CALL .print_number

HLT

data_area:
.D: DB 232
.add1_0: DB 0
.add1_x: DB 0
.add2_0: DB 0
.add2_y: DB 0
.main_0C: DB "10+56="
DB 0
