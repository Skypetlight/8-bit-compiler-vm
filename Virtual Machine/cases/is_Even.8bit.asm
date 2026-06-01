start:
	JMP .main
code_area:
.isEven:	POP A
	POP B
	MOV [.isEven_0], A
	MOV [.isEven_n], B
	PUSH [.isEven_n]
	MOV B, 2
	POP A
	MOV C, A
	DIV B
	MUL B
	MOV D, A
	MOV A, C
	SUB A, D
	PUSH A
	MOV B, 0
	POP A
	CMP A, B
	JE .accept_0
	JMP .reject_0

.accept_0:
	MOV A, 1
	MOV B, [.isEven_0]
	PUSH A
	PUSH B
	RET

.reject_0:
	MOV A, 0
	MOV B, [.isEven_0]
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
	PUSH 8
	CALL .isEven
	CALL .print_boolean

HLT

data_area:
.D: DB 232
.isEven_0: DB 0
.isEven_n: DB 0
.main_0C: DB "isEven(8)="
DB 0

.true: DB "true "
DB 0
.false: DB "false "
DB 0
