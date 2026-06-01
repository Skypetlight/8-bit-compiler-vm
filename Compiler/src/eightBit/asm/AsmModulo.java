package eightBit.asm;

import java.io.PrintStream;

public final class AsmModulo implements AsmAst {

	@Override
	public void genCode(PrintStream out) {
		// RHS -> B, LHS -> A (POP B then POP A)
        new AsmOneAddress("B", "POP").genCode(out);
        new AsmOneAddress("A", "POP").genCode(out);

        // C = dividend
        new AsmTwoAddress("C", "A", "MOV").genCode(out);

        // A = A / B
        new AsmOneAddress("B", "DIV").genCode(out);

        // A = A * B
        new AsmOneAddress("B", "MUL").genCode(out);

        // D = product
        new AsmTwoAddress("D", "A", "MOV").genCode(out);

        // A = dividend - product
        new AsmTwoAddress("A", "C", "MOV").genCode(out);
        new AsmTwoAddress("A", "D", "SUB").genCode(out);

        // push remainder
        new AsmOneAddress("A", "PUSH").genCode(out);
	}

}