start:
	JMP .main
code_area:
.devilsnumber:	POP A
	MOV [.devilsnumber_0], A
	MOV A, .devilsnumber_0C
	MOV B, [.devilsnumber_0]
	PUSH A
	PUSH B
	RET

.salute:	POP A
	MOV [.salute_0], A
	CALL .devilsnumber
	POP A
	MOV B, [.salute_0]
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
	CALL .salute
	CALL .print_string

HLT

data_area:
.D: DB 232
.devilsnumber_0C: DB "Hello 666!"
DB 0

.devilsnumber_0: DB 0
.salute_0: DB 0
