package eightBit.registry;

import eightBit.asm.AsmAst;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class AsmFormatter {

	private static final ThreadLocal<AsmFormatter> CTX = ThreadLocal.withInitial(AsmFormatter::new);

	private boolean PS;
	private boolean PB;
	private boolean PN;
	private List<AsmAst> dataArea;
	private List<AsmAst> codeArea;
	private List<AsmAst> mainArea;

	private AsmFormatter() {
		resetInstance();
	}

	public static void reset() {
		CTX.get().resetInstance();
	}

	private static AsmFormatter ctx() {
		return CTX.get();
	}

	private void resetInstance() {
		PS = false;
		PB = false;
		PN = false;
		dataArea = new ArrayList<>();
		codeArea = new ArrayList<>();
		mainArea = new ArrayList<>();
	}

	public static void setPS(boolean v) {
		ctx().PS = v;
	}

	public static boolean getPS() {
		return ctx().PS;
	}

	public static void setPN(boolean v) {
		ctx().PN = v;
	}

	public static boolean getPN() {
		return ctx().PN;
	}

	public static void setPB(boolean v) {
		ctx().PB = v;
	}

	public static boolean getPB() {
		return ctx().PB;
	}

	public static void addData(AsmAst a) {
		ctx().dataArea.add(a);
	}

	public static void addCode(AsmAst a) {
		ctx().codeArea.add(a);
	}

	public static void addMain(AsmAst a) {
		ctx().mainArea.add(a);
	}

	public static void genData(PrintStream out) {
		ctx().dataArea.forEach(a -> {
			a.genCode(out);
			out.println();
		});
	}

	public static void genCode(PrintStream out) {
		ctx().codeArea.forEach(a -> {
			a.genCode(out);
			out.println();
		});
	}

	public static void genMain(PrintStream out) {
		ctx().mainArea.forEach(a -> {
			a.genCode(out);
			out.println();
		});
	}

	public static void clearContext() {
		CTX.remove();
	}

}
