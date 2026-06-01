package eightBit.asm;

import java.io.PrintStream;

public class AsmOneAddress extends AsmInstruction {

	protected Object arg;

	public AsmOneAddress(Object a, String instruction) {
		super(new AsmID(""));
		this.arg = a;
		((AsmID) this.operation).setValue(instruction);
	}

	@Override
	public void genCode(PrintStream out) {
		out.print("\t");
		operation.genCode(out);
		out.print(" ");
		out.println(this.arg);
	}

}
