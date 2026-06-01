package eightBit.asm;

import eightBit.registry.AsmFormatter;
import eightBit.registry.SymbolTable;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class AsmFunction implements AsmAst {

	private AsmID ID;
	private List<AsmAst> prologue;
	private AsmAst body;

	public AsmFunction(AsmID l, List<AsmAst> p, AsmAst b) {
		this.ID = l;
		this.prologue = p;
		this.body = b;

		AsmFormatter.addData(new AsmID("." + this.ID.getValue() + "_0: DB 0"));
		SymbolTable.addVariable("0", 0, -1);

		prologue.forEach(a -> {
			((AsmBlock) a).getMembers().forEach(e -> {
				AsmFormatter.addData(new AsmID("." + this.ID.getValue() + "_" + ((AsmID) e).getValue() + ": DB 0"));
				SymbolTable.addVariable(((AsmID) e).getValue(), 0, -1);
			});
		});
	}

	private static IntStream revRange(int from, int to) {
		return IntStream.range(from, to).map(i -> to - i + from - 1);
	}

	@Override
	public void genCode(PrintStream out) {
		List<String> registries = new ArrayList<>(Arrays.asList("A", "B", "C", "D"));
		String fnCtx = "." + this.ID.getValue();
		List<String> variables = SymbolTable.getVariablesToUse(fnCtx, -1);
		boolean preserve = SymbolTable.mustPreserveFrame(fnCtx);

		new AsmID("." + this.ID.getValue() + ":").genCode(out);
		new AsmOneAddress(registries.get(0), "POP").genCode(out);
		IntStream.range(1, variables.size()).forEach(i -> new AsmOneAddress(registries.get(i), "POP").genCode(out));

		if (preserve) {
			revRange(0, variables.size())
					.forEach(i -> new AsmOneAddress("[." + this.ID.getValue() + "_" + variables.get(i) + "]", "PUSH")
							.genCode(out));
		}

		new AsmTwoAddress("[." + this.ID.getValue() + "_" + variables.get(0) + "]", registries.get(0), "MOV")
				.genCode(out);
		IntStream.range(1, variables.size())
				.forEach(e -> new AsmTwoAddress("[." + this.ID.getValue() + "_" + variables.get(e) + "]",
						registries.get(e), "MOV").genCode(out));
		if (this.body != null)
			this.body.genCode(out);
	}

}
