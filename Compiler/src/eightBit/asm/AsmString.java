package eightBit.asm;

import eightBit.registry.SymbolTable;
import java.io.PrintStream;

public class AsmString extends AsmAtom<String> {

	final public static AsmID o = new AsmID("DB");

	protected AsmAst name;
	protected AsmAst operation;

	public AsmString(String value) {
		super(value);
		if (value.equals("null")) {
			this.setValue("\"undefined\"");
		}
		this.name = new AsmID(SymbolTable.addConstant(value, SymbolTable.getCallContext()));
		this.operation = o;
	}

	@Override
	public void genCode(PrintStream out) {
		out.print(this.context);
		out.print("_");
		this.name.genCode(out);
		out.print(": ");
		this.operation.genCode(out);
		out.println(" " + this.value);
		this.operation.genCode(out);
		out.print(" ");
		out.println(0);
	}

}
