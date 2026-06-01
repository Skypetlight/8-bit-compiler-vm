start:
	JMP .main
code_area:
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

main_area:
.main:
	PUSH .main_0C
	CALL .print_string

HLT

data_area:
.D: DB 232
.main_0C: DB "Hello World!"
DB 0
