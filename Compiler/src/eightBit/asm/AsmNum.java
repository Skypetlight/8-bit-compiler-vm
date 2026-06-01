package eightBit.asm;

import java.io.PrintStream;

public class AsmNum extends AsmAtom<Integer> {

	private AsmAst push;

	public AsmNum(Integer value) {
		super(value);
		this.push = new AsmOneAddress(value, "PUSH");
	}

	@Override
	public void genCode(PrintStream out) {
		this.push.genCode(out);
	}

}
