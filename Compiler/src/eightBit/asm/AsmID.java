package eightBit.asm;

import java.io.PrintStream;

public class AsmID extends AsmAtom<String> {

	public AsmID(String value) {
		super(value);
	}

	@Override
	public void genCode(PrintStream out) {
		out.print(this.value);
	}

}
