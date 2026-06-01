package eightBit.asm;

import java.io.PrintStream;

public class AsmEmpty implements AsmAst {

	@Override
	public void genCode(PrintStream out) {
		out.println(";empty statement!");
	}

}
