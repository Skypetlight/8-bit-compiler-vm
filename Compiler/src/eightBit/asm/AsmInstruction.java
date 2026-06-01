package eightBit.asm;

public class AsmInstruction implements AsmAst {

	protected AsmAst operation;

	public AsmInstruction(AsmAst o) {
		this.operation = o;
	}

}
