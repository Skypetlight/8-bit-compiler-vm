package eightBit.asm;

import java.io.PrintStream;

public class AsmOneOperation extends AsmOneAddress {

	public AsmOneOperation(Object arg, String instruction) {
		super(arg, instruction);
	}

	/*
	 * Stack convention for binary monom ops (*, /):
	 * - LHS is below RHS on the stack: [..., LHS, RHS]
	 * - POP RHS into register (usually B), POP LHS into A, then execute A <op> RHS.
	 */
	@Override
	public void genCode(PrintStream out) {

		if (this.arg instanceof String) {
			new AsmOneAddress(this.arg, "POP").genCode(out);
		}
		new AsmOneAddress("A", "POP").genCode(out);
		super.genCode(out);
		new AsmOneAddress("A", "PUSH").genCode(out);
	}

}
