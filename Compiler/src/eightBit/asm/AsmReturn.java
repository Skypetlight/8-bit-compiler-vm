package eightBit.asm;

import eightBit.registry.SymbolTable;
import java.io.PrintStream;
import java.util.List;

public class AsmReturn implements AsmAst {

	private String context;
	private AsmAst e;

	public AsmReturn(AsmAst e) {
		this.context = SymbolTable.getContext();
		this.e = e;
	}

	@Override
	public boolean isTerminal() {
		return true;
	}

	private void genReturnCode(PrintStream out, List<String> v) {
		new AsmOneAddress("A", "POP").genCode(out);
		if (v.isEmpty()) {
			new AsmOneAddress("A", "PUSH").genCode(out);
			new AsmID("\tRET\n").genCode(out);
			return;
		}
		new AsmTwoAddress("B", "[" + this.context + "_" + v.get(0) + "]", "MOV").genCode(out);

		if (SymbolTable.mustPreserveFrame(this.context)) {
			v.forEach(e -> {
				new AsmOneAddress("C", "POP").genCode(out);
				new AsmTwoAddress("[" + this.context + "_" + e + "]", "C", "MOV").genCode(out);
			});
		}
		new AsmOneAddress("A", "PUSH").genCode(out);
		new AsmOneAddress("B", "PUSH").genCode(out);
		new AsmID("\tRET\n").genCode(out);
	}

	@Override
	public void genCode(PrintStream out) {
		List<String> lets, variables = SymbolTable.getVariablesToUse(this.context, -1);
		String constant, variable, let;
		if (this.e instanceof AsmID) {
			if (((AsmID) this.e).getValue() instanceof String) {
				constant = SymbolTable.findConstantByValue(this.context, ((AsmID) this.e).getValue());
				if (constant != null) {
					new AsmOneAddress(this.context + "_" + constant, "PUSH").genCode(out);
				} else {
					variable = SymbolTable.findVariableByValue(this.context, ((AsmID) this.e).getValue());
					if (variable != null) {
						new AsmOneAddress(this.context + "_" + variable, "PUSH").genCode(out);
					} else {
						lets = SymbolTable.getVariablesToUse(this.context, -2);
						if (!lets.isEmpty()) {
							let = lets.stream().filter(v -> v.equals(((AsmID) this.e).getValue())).findFirst().get();
							new AsmOneAddress("[" + this.context + "_" + let + "]", "PUSH").genCode(out);
						} else {
							lets = SymbolTable.getVariablesToUse(this.context, -1);
							if (!lets.isEmpty()) {
								let = lets.stream().filter(v -> v.equals(((AsmID) this.e).getValue())).findFirst()
										.get();
								new AsmOneAddress("[" + this.context + "_" + let + "]", "PUSH").genCode(out);
							} else {
								throw new IllegalStateException("Intenta retornar algo que no existe!!!");
							}
						}
					}
				}
				genReturnCode(out, variables);
			}
		} else {
			this.e.genCode(out);
			genReturnCode(out, variables);
		}
	}

}
