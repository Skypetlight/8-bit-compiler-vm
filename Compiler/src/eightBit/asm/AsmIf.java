package eightBit.asm;

import eightBit.registry.SymbolTable;
import java.io.PrintStream;

public class AsmIf implements AsmAst {

	private AsmAst previous;
	private AsmAst condition;
	private AsmAst accept;
	private AsmAst reject;

	public AsmIf(AsmAst p, AsmAst c, AsmAst a, AsmAst r) {
		this.previous = p;
		this.condition = c;
		this.accept = a;
		this.reject = r;
	}

	@Override
	public void genCode(PrintStream out) {
		int num = SymbolTable.getIfNumber();

		this.previous.genCode(out);
		new AsmOneAddress("B", "POP").genCode(out);
		new AsmOneAddress("A", "POP").genCode(out);
		new AsmTwoAddress("A", "B", "CMP").genCode(out);

		// Condition prints JE/JNZ/etc without the target label
		this.condition.genCode(out);
		new AsmID(" .accept_" + num + "\n").genCode(out);
		new AsmOneAddress(".reject_" + num, "JMP").genCode(out);

		// Detect whether "else" exists
		boolean hasElse = !(reject instanceof AsmID) || !((AsmID) reject).getValue().isEmpty();
		boolean acceptTerminal = (accept != null) && accept.isTerminal();

		new AsmID("\n.accept_" + num + ":\n").genCode(out);
		this.accept.genCode(out);

		// Only jump over else when there IS an else AND the accept branch can fall through
		if (hasElse && !acceptTerminal)
			new AsmOneAddress(".ifend_" + num, "JMP").genCode(out);

		new AsmID("\n.reject_" + num + ":\n").genCode(out);
		this.reject.genCode(out);

		// Only emit join label when it can actually be reached
		if (hasElse && !acceptTerminal)
			new AsmID("\n.ifend_" + num + ":\n").genCode(out);
	}

}
