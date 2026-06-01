start:
	JMP .main
code_area:
.assign:	POP A
	MOV [.assign_0], A
	MOV A, .assign_0C
	MOV [.assign_y], A
	MOV A, [.assign_y]
	MOV B, [.assign_0]
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

main_area:
.main:
	CALL .assign
	CALL .print_string

HLT

data_area:
.D: DB 232
.assign_0C: DB "Hello 666!"
DB 0
.assign_y: DB 0
.assign_0: DB 0
