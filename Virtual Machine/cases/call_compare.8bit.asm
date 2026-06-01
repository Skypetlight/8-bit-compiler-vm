start:
	JMP .main
code_area:
.compare:	POP A
	POP B
	POP C
	MOV [.compare_0], A
	MOV [.compare_x], B
	MOV [.compare_y], C
	MOV A, [.compare_x]
	MOV B, [.compare_y]
	CMP A, B
	JA .accept_0
	JMP .reject_0

.accept_0:
	MOV A, 1
	MOV B, [.compare_0]
	PUSH A
	PUSH B
	RET

.reject_0:
	MOV A, 0
	MOV B, [.compare_0]
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
	PUSH 10
	CALL .compare
	CALL .print_boolean
	PUSH .main_1C
	CALL .print_string
	PUSH 10
	PUSH 5
	CALL .compare
	CALL .print_boolean

HLT

data_area:
.D: DB 232
.compare_0: DB 0
.compare_x: DB 0
.compare_y: DB 0
.main_0C: DB "10>5="
DB 0

.true: DB "true "
DB 0
.false: DB "false "
DB 0
.main_1C: DB " 5>10="
DB 0
