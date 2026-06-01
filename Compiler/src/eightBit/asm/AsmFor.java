package eightBit.asm;

import eightBit.registry.SymbolTable;
import java.io.PrintStream;

public class AsmFor implements AsmAst {

	private AsmAst previous;
	private AsmAst init;
	private AsmAst loop;
	private AsmAst condition;
	private AsmAst body;

	public AsmFor(AsmAst p, AsmAst i, AsmAst c, AsmAst l, AsmAst b) {
		this.previous = p;
		this.init = i;
		this.condition = c;
		this.loop = l;
		this.body = b;
	}

	@Override
	public void genCode(PrintStream out) {
		int num = SymbolTable.getForNumber();
		this.init.genCode(out);
		new AsmID(".for_" + num + ":\n").genCode(out);
		this.previous.genCode(out);
		new AsmOneAddress("B", "POP").genCode(out);
		new AsmOneAddress("A", "POP").genCode(out);
		new AsmTwoAddress("A", "B", "CMP").genCode(out);
		this.condition.genCode(out);
		new AsmID(" .for_body_" + num + "\n").genCode(out);
		new AsmOneAddress(".for_exit_" + num + "\n", "JMP").genCode(out);
		new AsmID(".for_body_" + num + ":\n").genCode(out);
		this.body.genCode(out);
		this.loop.genCode(out);
		new AsmOneAddress(".for_" + num + "\n", "JMP").genCode(out);
		new AsmID(".for_exit_" + num + ":\n").genCode(out);
	}

}
