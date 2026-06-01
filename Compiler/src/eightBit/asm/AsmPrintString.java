package eightBit.asm;

import java.io.PrintStream;

public class AsmPrintString implements AsmAst {

	@Override
	public void genCode(PrintStream out) {
		out.println(".print_string:");
		out.println("\tMOV D, [.D]");
		out.println("\tPOP A");
		out.println("\tPOP B");
		out.println("\tPUSH A");
		out.println("\tMOV A, 0");
		out.println(".print_string_method:");
		out.println("\tMOV C, [B]");
		out.println("\tMOV [D], C");
		out.println("\tINC B");
		out.println("\tINC D");
		out.println("\tCMP A, [B]");
		out.println("\tJNZ .print_string_method");
		out.println(".print_string_exit:");
		out.println("\tMOV [.D], D");
		out.println("\tRET");
	}

}
