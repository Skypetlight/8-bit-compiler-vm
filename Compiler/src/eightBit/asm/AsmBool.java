package eightBit.asm;

import java.io.PrintStream;

public class AsmBool extends AsmAtom<Integer> {

	private AsmAst push;

	public AsmBool(boolean value) {
		super(0);

		if (value) {
			this.setValue(1);
		} else {
			this.setValue(0);
		}

		this.push = new AsmOneAddress(this.value, "PUSH");
	}

	@Override
	public void genCode(PrintStream out) {
		this.push.genCode(out);
	}

}
