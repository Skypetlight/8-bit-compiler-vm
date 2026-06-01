package eightBit.asm;

import java.io.PrintStream;
import java.util.List;

public class AsmBlock implements AsmAst {

	protected List<AsmAst> members;

	public AsmBlock(List<AsmAst> members) {
		this.members = members;
	}

	public List<AsmAst> getMembers() {
		return this.members;
	}

	public void addMember(AsmAst m) {
		this.members.add(m);
	}

	@Override
	public boolean isTerminal() {
		if (members == null || members.isEmpty())
			return false;
		return members.get(members.size() - 1).isTerminal();
	}

	@Override
	public void genCode(PrintStream out) {
		this.members.forEach(t -> t.genCode(out));
	}

}
