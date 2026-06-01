package eightBit.asm;

import java.io.PrintStream;

public class AsmTwoAddress extends AsmInstruction {

	protected String left;
	protected Object right;

	public AsmTwoAddress(String left, Object right, String instruction) {
		super(new AsmID(""));
		this.left = left;
		this.right = right;
		((AsmID) this.operation).setValue(instruction);
	}

	public AsmAst getOper() {
		return this.operation;
	}

	public String getLeft() {
		return this.left;
	}

	public Object getRight() {
		return this.right;
	}

	@Override
	public void genCode(PrintStream out) {
		out.print("\t");
		this.operation.genCode(out);
		out.print(" ");
		out.print(this.left);
		out.print(", ");
		out.println(this.right);
	}

}
