package eightBit.asm;

import eightBit.registry.SymbolTable;

public class AsmAtom<T> implements AsmAst {

	protected String context;
	protected T value;

	public AsmAtom(T value) {
		this.context = SymbolTable.getContext();
		this.value = value;
	}

	public String getContext() {
		return this.context;
	}

	public void setContext(String c) {
		this.context = c;
	}

	public T getValue() {
		return this.value;
	}

	public void setValue(T value) {
		this.value = value;
	}

}
