package eightBit.asm;

import java.io.PrintStream;

public class AsmTwoOperation extends AsmTwoAddress {

	public AsmTwoOperation(String left, Object right, String instruction) {
		super(left, right, instruction);
	}

	@Override
	public void genCode(PrintStream out) {
		if (this.right instanceof String) {
			new AsmOneAddress(this.right, "POP").genCode(out);
		}
		new AsmOneAddress(this.left, "POP").genCode(out);
		super.genCode(out);
		new AsmOneAddress(this.left, "PUSH").genCode(out);
	}

}
