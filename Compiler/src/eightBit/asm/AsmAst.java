package eightBit.asm;

import java.io.PrintStream;

public interface AsmAst {

	default void genCode() {
		genCode(System.out);
	}

	default void genCode(PrintStream out) {

	}

	default boolean isTerminal() {
        return false;
    }

}
