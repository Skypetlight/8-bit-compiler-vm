package eightBit.asm;

import eightBit.registry.SymbolTable;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class AsmCall implements AsmAst {

	final public static AsmID o = new AsmID("\tCALL");
	private String context;
	private AsmAst funName;
	private List<AsmAst> args;

	public AsmCall(AsmAst funName, List<AsmAst> args) {
		this.context = SymbolTable.getContext();
		this.funName = funName;
		this.args = args;
	}

	public AsmCall(AsmAst funName, AsmAst e) {
		this(funName, Arrays.asList(e));
	}

	/*
	 * Calling convention:
	 * - CALL pushes return address.
	 * - Callee POPs return address first, then POPs arguments left-to-right into
	 * parameters.
	 * Therefore the caller must PUSH arguments right-to-left.
	 */
	@Override
	public void genCode(PrintStream out) {
		List<AsmAst> flatArgs = args.stream()
				.flatMap(node -> node instanceof AsmBlock block
						? block.getMembers().stream()
						: Stream.of(node))
				.collect(Collectors.toList());

		// Java 9+: iterate with a predicate for reverse traversal
		IntStream.iterate(flatArgs.size() - 1, i -> i >= 0, i -> i - 1)
				.mapToObj(flatArgs::get)
				.forEach(v -> {
					if (v instanceof AsmID id) {
						String text = id.getValue();

						if (SymbolTable.findVariable(context, text) != null) {
							new AsmOneAddress("[" + context + "_" + text + "]", "PUSH").genCode(out);
							return;
						}

						String constName = SymbolTable.findConstantByValue(context, text);
						if (constName != null) {
							new AsmOneAddress(context + "_" + constName, "PUSH").genCode(out);
							return;
						}
					}

					v.genCode(out);
				});

		o.genCode(out);
		out.print(" .");
		this.funName.genCode(out);
		out.println();
	}

}
