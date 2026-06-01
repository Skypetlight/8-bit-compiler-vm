package eightBit.asm;

import eightBit.registry.SymbolTable;
import java.io.PrintStream;

public class AsmAssign implements AsmAst {

	private AsmAst previous;
	private String context;
	protected Object arg;
	private String name;
	private String content;

	public String getContext() {
		return context;
	}

	public String getName() {
		return name;
	}

	public Object getArg() {
		return arg;
	}

	public String getContent() {
		return content;
	}

	public AsmAssign(AsmAst p, String n, Object a, String c) {
		this.context = SymbolTable.getContext();
		this.previous = p;
		this.name = n;
		this.arg = a;
		this.content = c;
	}

	@Override
	public void genCode(PrintStream out) {
		String label;

		this.previous.genCode(out);
		if (this.content.equals("")) {
			new AsmTwoAddress("[" + this.context + "_" + this.name + "]", this.arg, "MOV").genCode(out);
		} else {
			if (this.content.contains("\"")) {
				label = SymbolTable.findConstantByValue(this.context, this.content);
				new AsmTwoAddress("A", this.context + "_" + label, "MOV").genCode(out);
				new AsmTwoAddress("[" + this.context + "_" + this.name + "]", "A", "MOV").genCode(out);
			} else {
				label = "[" + this.context + "_" + this.content + "]";
				new AsmTwoAddress("A", label, "MOV").genCode(out);
				new AsmTwoAddress("[" + this.context + "_" + this.name + "]", "A", "MOV").genCode(out);
			}
		}
	}
}
