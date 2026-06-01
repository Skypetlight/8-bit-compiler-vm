package eightBit.asm;

import java.io.PrintStream;

public class AsmPrintBoolean implements AsmAst {

	@Override
	public void genCode(PrintStream out) {out.println(".print_boolean:");
        out.println("\tPOP A");
        out.println("\tPOP B");
        out.println("\tCMP B, 0");
        out.println("\tJZ .false_case");
        out.println(".true_case:");
        out.println("\tPUSH .true");
        out.println("\tPUSH A");
        out.println("\tJMP .print_string");
        out.println(".false_case:");
        out.println("\tPUSH .false");
        out.println("\tPUSH A");
        out.println("\tJMP .print_string");
	}

}
