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

.salute:	POP A
	POP B
	MOV [.salute_0], A
	MOV [.salute_s], B
	PUSH [.salute_s]
	CALL .print_string
	MOV B, [.salute_0]
	PUSH B
	RET

main_area:
.main:
	PUSH .main_0C
	CALL .salute

HLT

data_area:
.D: DB 232
.salute_0: DB 0
.salute_s: DB 0
.main_0C: DB "Hello 666!"
DB 0
