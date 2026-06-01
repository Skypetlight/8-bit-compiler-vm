package eightBit.asm;

import java.io.PrintStream;

public class AsmMain implements AsmAst {

	private AsmID ID;
	private AsmAst body;

	public AsmMain(AsmID l, AsmAst b) {
		this.ID = l;
		this.body = b;
	}

	@Override
	public void genCode(PrintStream out) {
		out.println("." + this.ID.getValue() + ":");

		if (this.body != null) {
			this.body.genCode(out);
		}

		new AsmID("\nHLT\n").genCode(out);
	}

}
