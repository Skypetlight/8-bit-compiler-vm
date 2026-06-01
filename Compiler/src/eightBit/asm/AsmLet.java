package eightBit.asm;

import java.io.PrintStream;

public class AsmLet implements AsmAst {

	private AsmAst assignments;
	private AsmAst body;

	public AsmLet(AsmAst assignments, AsmAst closedStatements) {
		this.assignments = assignments;
		this.body = closedStatements;
	}

	@Override
	public void genCode(PrintStream out) {
		((AsmBlock) this.assignments).getMembers().forEach(a -> a.genCode(out));
		this.body.genCode(out);
	}

}
