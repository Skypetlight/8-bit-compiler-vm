start:
	JMP .main
code_area:
.deMorgan:	POP A
	POP B
	POP C
	MOV [.deMorgan_0], A
	MOV [.deMorgan_p], B
	MOV [.deMorgan_q], C
	MOV A, [.deMorgan_q]
	MOV B, [.deMorgan_0]
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

.print_boolean:
	POP A
	POP B
	CMP B, 0
	JZ .false_case
.true_case:
	PUSH .true
	PUSH A
	JMP .print_string
.false_case:
	PUSH .false
	PUSH A
	JMP .print_string

main_area:
.main:
	PUSH .main_0C
	CALL .print_string
	PUSH 1
	PUSH 0
	CALL .deMorgan
	CALL .print_boolean

HLT

data_area:
.D: DB 232
.deMorgan_0: DB 0
.deMorgan_p: DB 0
.deMorgan_q: DB 0
.main_0C: DB "deMorgan(false, true)="
DB 0

.true: DB "true "
DB 0
.false: DB "false "
DB 0
