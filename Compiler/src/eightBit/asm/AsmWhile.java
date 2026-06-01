package eightBit.asm;

import eightBit.registry.SymbolTable;
import java.io.PrintStream;

public class AsmWhile implements AsmAst {

	private AsmAst previous;
	private AsmAst condition;
	private AsmAst body;

	public AsmWhile(AsmAst p, AsmAst c, AsmAst b) {
		this.previous = p;
		this.condition = c;
		this.body = b;
	}

	@Override
	public void genCode(PrintStream out) {
		int num = SymbolTable.getWhileNumber();
		new AsmID(".while_" + num + ":\n").genCode(out);
		this.previous.genCode(out);
		new AsmOneAddress("B", "POP").genCode(out);
		new AsmOneAddress("A", "POP").genCode(out);
		new AsmTwoAddress("A", "B", "CMP").genCode(out);
		this.condition.genCode(out);
		new AsmID(" .while_body_" + num + "\n").genCode(out);
		new AsmOneAddress(".while_exit_" + num + "\n", "JMP").genCode(out);
		new AsmID(".while_body_" + num + ":\n").genCode(out);
		this.body.genCode(out);
		new AsmOneAddress(".while_" + num + "\n", "JMP").genCode(out);
		new AsmID(".while_exit_" + num + ":\n").genCode(out);
	}

}
