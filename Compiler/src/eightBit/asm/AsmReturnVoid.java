package eightBit.asm;

import java.io.PrintStream;
import java.util.List;

import eightBit.registry.SymbolTable;

/**
 * Restores function-saved locals and returns without producing a value on the
 * stack.
 * Used for functions with no explicit `return expr`.
 *
 * IMPORTANT:
 * This must restore the function-definition frame (callNumber = -1), NOT the
 * last call-site context.
 */
public class AsmReturnVoid implements AsmAst {

	private final String context;

	public AsmReturnVoid() {
		this.context = SymbolTable.getContext();
	}

	@Override
	public boolean isTerminal() {
		return true;
	}

	@Override
	public void genCode(PrintStream out) {
		// Must use function-definition context, not "last call context".
		List<String> variables = SymbolTable.getVariablesToUse(this.context, -1);

		if (variables.isEmpty()) {
			throw new IllegalStateException(
					"AsmReturnVoid: no variables for context=" + context + " (expected at least return slot '0')");
		}

		// Return address is always stored in .<fn>_0 by the prologue.
		new AsmTwoAddress("B", "[" + this.context + "_0]", "MOV").genCode(out);

		if (SymbolTable.mustPreserveFrame(this.context)) {
			variables.forEach(v -> {
				new AsmOneAddress("C", "POP").genCode(out);
				new AsmTwoAddress("[" + this.context + "_" + v + "]", "C", "MOV").genCode(out);
			});
		}

		// Return with no value left on stack.
		new AsmOneAddress("B", "PUSH").genCode(out);
		new AsmID("\tRET\n").genCode(out);
	}

}