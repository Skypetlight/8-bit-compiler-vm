package eightBit.asm;

import java.io.PrintStream;

public class AsmPrintNumber implements AsmAst {

	@Override
	public void genCode(PrintStream out) {
		out.println(".print_number:");
		out.println("\tMOV D, [.D]");
		out.println("\tPOP C");
		out.println("\tPOP A");
		out.println("\tPUSH 255");
		out.println("\tPUSH A");
		out.println(".print_int:");
		out.println("\tDIV 10");
		out.println("\tPOP B");
		out.println("\tPUSH A");
		out.println("\tMUL 10");
		out.println("\tSUB B, A");
		out.println("\tPOP A");
		out.println("\tPUSH B");
		out.println("\tCMP A, 0");
		out.println("\tJZ .print_int_method");
		out.println("\tPUSH A");
		out.println("\tJMP .print_int");
		out.println(".print_int_method:");
		out.println("\tPOP A");
		out.println("\tCMP A, 255");
		out.println("\tJZ .print_int_exit");
		out.println("\tADD A, 48");
		out.println("\tMOV [D], A");
		out.println("\tINC D");
		out.println("\tJMP .print_int_method");
		out.println(".print_int_exit:");
		out.println("\tMOV [.D], D");
		out.println("\tPUSH C");
		out.println("\tRET");
	}

}
