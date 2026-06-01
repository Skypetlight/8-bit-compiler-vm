start:
	JMP .main
code_area:
.gausWhile:	POP A
	POP B
	MOV [.gausWhile_0], A
	MOV [.gausWhile_n], B
	MOV [.gausWhile_s], 0
	MOV A, [.gausWhile_n]
	MOV [.gausWhile_i], A
.while_0:
	MOV A, [.gausWhile_i]
	MOV B, 0
	CMP A, B
	JNZ .while_body_0
	JMP .while_exit_0

.while_body_0:
	MOV A, [.gausWhile_s]
	ADD A, [.gausWhile_n]
	MOV [.gausWhile_s], A
	MOV A, [.gausWhile_i]
	SUB A, 1
	MOV [.gausWhile_i], A
	JMP .while_0

.while_exit_0:
	PUSH [.gausWhile_s]
	PUSH [.gausWhile_n]
	CALL .gaussFormula
	POP B
	POP A
	CMP A, B
	JE .accept_0
	JMP .reject_0

.accept_0:
	MOV A, 1
	MOV B, [.gausWhile_0]
	PUSH A
	PUSH B
	RET

.reject_0:
	MOV A, 0
	MOV B, [.gausWhile_0]
	PUSH A
	PUSH B
	RET

.gaussFormula:	POP A
	POP B
	MOV [.gaussFormula_0], A
	MOV [.gaussFormula_n], B
	PUSH [.gaussFormula_n]
	MOV A, [.gaussFormula_n]
	ADD A, 1
	PUSH A
	POP B
	POP A
	MUL B
	PUSH A
	MOV B, 2
	POP A
	DIV B
	MOV B, [.gaussFormula_0]
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
	PUSH 5
	CALL .gausWhile
	CALL .print_boolean

HLT

data_area:
.D: DB 232
.gausWhile_s: DB 0
.gausWhile_i: DB 0
.gausWhile_0: DB 0
.gausWhile_n: DB 0
.gaussFormula_0: DB 0
.gaussFormula_n: DB 0
.main_0C: DB "gauss(5)="
DB 0

.true: DB "true "
DB 0
.false: DB "false "
DB 0
