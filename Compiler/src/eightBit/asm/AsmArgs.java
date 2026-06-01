package eightBit.asm;

import java.io.PrintStream;
import java.util.List;

public class AsmArgs implements AsmAst {

	private List<AsmAst> exps;

	public AsmArgs(List<AsmAst> exps) {
		this.exps = exps;
	}

	@Override
	public void genCode(PrintStream out) {
		this.exps.forEach(a -> a.genCode(out));
	}

}
