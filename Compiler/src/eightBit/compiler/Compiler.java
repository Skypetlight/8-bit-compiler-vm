package eightBit.compiler;

import eightBit.antlr.EightBitBaseVisitor;
import eightBit.antlr.EightBitParser;
import eightBit.asm.AsmAssign;
import eightBit.asm.AsmAst;
import eightBit.asm.AsmBlock;
import eightBit.asm.AsmCall;
import eightBit.asm.AsmID;
import eightBit.asm.AsmNum;
import eightBit.asm.AsmReturnVoid;
import eightBit.asm.AsmTwoAddress;
import eightBit.registry.AsmFormatter;
import eightBit.registry.SymbolTable;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.swing.plaf.nimbus.State;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

public class Compiler extends EightBitBaseVisitor<AsmAst> implements AsmEmitter {

	protected AsmAst program;
	protected List<AsmAst> statements;

	private boolean sawReturnInCurrentFunction = false;

	public Compiler() {
		this.statements = new ArrayList<>();
		this.program = PROGRAM(this.statements);
	}

	public AsmAst getProgram() {
		return this.program;
	}

	public void genCode() {
		program.genCode(System.out);
	}

	public void genCode(PrintStream out) {
		program.genCode(out);
	}

	public AsmAst compile(ParseTree tree) {
		SymbolTable.reset();
		AsmFormatter.reset();
		this.statements = new ArrayList<>();
		this.program = PROGRAM(this.statements);
		this.sawReturnInCurrentFunction = false;
		return visit(tree);
	}

	@Override
	public AsmAst visitEightProgram(EightBitParser.EightProgramContext ctx) {
		AsmFormatter.addData(new AsmID(".D: DB 232"));
		if (ctx.eightFunction().stream().filter(e -> e.id().getText().equals("main")).count() != 1) {
			Token t = ctx.getStart();
			throw new CompilationException("Program doesn't have a main method", t.getLine(),
					t.getCharPositionInLine());
		}
		// Pre-pass: build call graph to detect which functions are recursive (directly
		// or mutually).
		Set<String> fnNames = ctx.eightFunction().stream()
				.map(f -> f.id().getText())
				.collect(Collectors.toSet());

		Map<String, Set<String>> edges = new HashMap<>();
		ctx.eightFunction().forEach(f -> edges.put(f.id().getText(), new HashSet<>()));

		new CallGraphVisitor(edges, fnNames).visit(ctx);

		Set<String> preserve = computeRecursiveContexts(edges).stream()
				.map(n -> "." + n)
				.collect(Collectors.toSet());

		SymbolTable.setFramePreserveContexts(preserve);

		ctx.eightFunction().forEach(fun -> visit(fun));
		return this.program = PROGRAM(this.statements);
	}

	@Override
	public AsmAst visitEightFunction(EightBitParser.EightFunctionContext ctx) {

		AsmID i = (AsmID) visit(ctx.id());

		SymbolTable.setContext(i.getValue());

		sawReturnInCurrentFunction = false;

		AsmAst p = visit(ctx.formals());

		AsmAst b = visit(ctx.funBody());

		if (!ctx.id().getText().equals("main") && !sawReturnInCurrentFunction) {
			AsmAst implicit = new AsmReturnVoid();
			if (b instanceof AsmBlock) {
				((AsmBlock) b).getMembers().add(implicit);
			} else {
				b = BLOCK(Arrays.asList(b, implicit));
			}
		}

		AsmAst function;

		if (ctx.id().getText().equals("main")) {
			function = MAIN(i, b);
			AsmFormatter.addMain(function);
		} else {
			function = FUNCTION(i, PROLOGUE(p), b);
			AsmFormatter.addCode(function);
		}

		this.statements.add(function);
		return function;
	}

	@Override
	public AsmAst visitFormals(EightBitParser.FormalsContext ctx) {
		EightBitParser.IdListContext idList = ctx.idList();
		return (idList == null) ? BLOCK() : visit(idList);
	}

	@Override
	public AsmAst visitIdList(EightBitParser.IdListContext ctx) {
		return BLOCK(ctx.id().stream().map(c -> visit(c)).collect(Collectors.toList()));
	}

	@Override
	public AsmAst visitId(EightBitParser.IdContext ctx) {
		return ID(ctx.ID().getText());
	}

	@Override
	public AsmAst visitFunBody(EightBitParser.FunBodyContext ctx) {
		return visitChildren(ctx);
	}

	@Override
	public AsmAst visitEmptyStatement(EightBitParser.EmptyStatementContext ctx) {
		return EMPTY();
	}

	@Override
	public AsmAst visitLetStatement(EightBitParser.LetStatementContext ctx) {
		AsmAst closedStatements = visit(ctx.closedStatement());

		EightBitParser.AssignStmtListContext listCtx = ctx.assignStmtList();
		if (listCtx != null) {
			ensureLetStringConstantsEmitted(listCtx);
			AsmAst assignments = visit(listCtx);

			((AsmBlock) assignments).getMembers().forEach(a -> {
				AsmAssign aux = (AsmAssign) a;
				AsmFormatter.addData(ID(aux.getContext() + "_" + aux.getName() + ": DB 0"));
				SymbolTable.addVariable(aux.getName(), 0, -2);
			});

			return LET(assignments, closedStatements);
		} else {
			Token t = ctx.getStart();
			throw new CompilationException("Let sin declaración", t.getLine(), t.getCharPositionInLine());
		}
	}

	@Override
	public AsmAst visitAssignStmtList(EightBitParser.AssignStmtListContext ctx) {
		List<AsmAst> assignments = ctx.assignStatement().stream().map(this::visit).collect(Collectors.toList());
		return ASSIGNLIST(assignments);
	}

	@Override
	public AsmAst visitClosedStatement(EightBitParser.ClosedStatementContext ctx) {
		return visitChildren(ctx);
	}

	@Override
	public AsmAst visitAssignStatement(EightBitParser.AssignStatementContext ctx) {
		AsmAst id = visit(ctx.id());
		String rhsText = ctx.expr().getText();

		// String literal assignment
		if (rhsText.contains("\"")) {
			return ASSIGN_STRING(id, rhsText, rhsText);
		}

		// Peephole: x = x +/- (NUM | id) => MOV A,[x]; ADD/SUB A,imm/[y]; MOV [x],A
		// Critical for this VM: keeps binary small and preserves stack headroom.
		AsmAst selfUpdate = tryCompileSelfUpdate(id, rhsText);
		if (selfUpdate != null) {
			return selfUpdate;
		}

		AsmAst exp = visit(ctx.expr());

		// Constant RHS
		if (exp instanceof AsmNum) {
			return ASSIGN_NUM(id, Integer.toString(((AsmNum) exp).getValue()), ID(""), "");
		}

		AsmBlock rhs = new AsmBlock(new ArrayList<>());

		if (exp instanceof AsmBlock) {
			rhs.getMembers().addAll(((AsmBlock) exp).getMembers());
		} else if (exp instanceof AsmID) {
			String name = ((AsmID) exp).getValue();
			rhs.addMember(PUSH("[" + SymbolTable.getContext() + "_" + name + "]"));
		} else if (exp instanceof AsmCall) {
			rhs.addMember(exp);
		} else if (exp != null) {
			rhs.addMember(exp);
		}

		// Most expressions push their result; POP into A before storing.
		rhs.addMember(POP("A"));
		return ASSIGN_NUM(id, "A", rhs, "");
	}

	@Override
	public AsmAst visitForStatement(EightBitParser.ForStatementContext ctx) {
		AsmAst initCondition = visit(ctx.assignStatement(0));

		AsmFormatter.addData(
				ID(((AsmAssign) initCondition).getContext() + "_" + ((AsmAssign) initCondition).getName() + ": DB 0"));
		SymbolTable.addVariable(((AsmAssign) initCondition).getName(), 0, -2);

		AsmAst block = visit(ctx.expr());
		AsmAst cmp = ((AsmBlock) block).getMembers().get(((AsmBlock) block).getMembers().size() - 1);
		((AsmBlock) block).getMembers().remove(cmp);
		maybeInvertConditionJump(ctx.expr().getText(), cmp);

		AsmAst loopCondition = visit(ctx.assignStatement(1));
		AsmAst body = visit(ctx.closedStatement());
		return FOR(block, initCondition, cmp, loopCondition, body);
	}

	@Override
	public AsmAst visitWhileStatement(EightBitParser.WhileStatementContext ctx) {
		AsmAst exprAst = visit(ctx.expr());
		AsmBlock exprBlock = requireBlock(exprAst, "while condition", ctx.getText());

		List<AsmAst> members = exprBlock.getMembers();
		if (members.isEmpty()) {
			throw new IllegalStateException("Empty codegen for while condition: " + ctx.getText());
		}

		AsmAst cmp = members.get(members.size() - 1);
		members.remove(members.size() - 1);
		maybeInvertConditionJump(ctx.expr().getText(), cmp);

		AsmAst body = visit(ctx.closedStatement());
		if (body == null) {
			body = BLOCK(); // defensive
		}

		return WHILE(exprBlock, cmp, body);
	}

	@Override
	public AsmAst visitIfStatement(EightBitParser.IfStatementContext ctx) {
		AsmAst block = visit(ctx.expr());

		AsmAst cmp = ((AsmBlock) block).getMembers().get(((AsmBlock) block).getMembers().size() - 1);
		((AsmBlock) block).getMembers().remove(cmp);
		maybeInvertConditionJump(ctx.expr().getText(), cmp);

		AsmAst accept = visit(ctx.closedStatement(0));
		AsmAst reject;
		if (ctx.closedStatement().size() == 2) {
			reject = visit(ctx.closedStatement(1));
		} else {
			reject = ID("");
		}
		return IF(block, cmp, accept, reject);
	}

	@Override
	public AsmAst visitCallStatement(EightBitParser.CallStatementContext ctx) {
		if ((ctx.ID().getText().equals("print_string")) && (!AsmFormatter.getPS())) {
			AsmFormatter.addCode(PRINTSTRING());
			AsmFormatter.setPS(true);
		}
		if ((ctx.ID().getText().equals("print_number")) && (!AsmFormatter.getPN())) {
			AsmFormatter.addCode(PRINTNUMBER());
			AsmFormatter.setPN(true);
		}
		if ((ctx.ID().getText().equals("print_boolean")) && (!AsmFormatter.getPB())) {
			if ((!AsmFormatter.getPS())) {
				AsmFormatter.addCode(PRINTSTRING());
				AsmFormatter.setPS(true);
			}
			AsmFormatter.addCode(PRINTBOOLEAN());
			AsmFormatter.setPB(true);
		}

		SymbolTable.setCallContext();

		AsmAst id = ID(ctx.ID().getText());

		AsmAst args = visit(ctx.arguments());

		return CALL(id, ARGS(args));
	}

	@Override
	public AsmAst visitReturnStatement(EightBitParser.ReturnStatementContext ctx) {
		sawReturnInCurrentFunction = true;
		AsmAst boolIf = tryCompileBoolReturnAsIf(ctx.expr());
		if (boolIf != null) {
			return boolIf;
		}
		return RETURN(visit(ctx.expr()));
	}

	@Override
	public AsmAst visitBlockStatement(EightBitParser.BlockStatementContext ctx) {
		EightBitParser.ClosedListContext closedList = ctx.closedList();
		return (closedList == null) ? BLOCK() : visit(closedList);
	}

	@Override
	public AsmAst visitClosedList(EightBitParser.ClosedListContext ctx) {
		return BLOCK(
				ctx.closedStatement().stream().map(this::visit).filter(Objects::nonNull).collect(Collectors.toList()));
	}

	@Override
	public AsmAst visitRelOperation(EightBitParser.RelOperationContext ctx) {
		// Grammar: relOperation : arithOperation (relOperator arithOperation)* | '!'
		// relOperation ;
		// If this node is a NOT, invert the comparison jump of the inner relOperation.
		if (ctx.relOperation() != null) {
			AsmAst inner = visit(ctx.relOperation());

			if (!(inner instanceof AsmBlock)) {
				throw new IllegalStateException("NOT expects relOperation to compile to AsmBlock: " + ctx.getText());
			}

			AsmBlock block = (AsmBlock) inner;
			if (block.getMembers().isEmpty()) {
				throw new IllegalStateException("Empty codegen for NOT: " + ctx.getText());
			}

			AsmAst last = block.getMembers().get(block.getMembers().size() - 1);
			if (!(last instanceof AsmID)) {
				throw new IllegalStateException(
						"NOT expects a conditional jump at end of relOperation: " + ctx.getText());
			}

			AsmID jmp = (AsmID) last;
			jmp.setValue(invertJump(jmp.getValue())); // JE<->JNZ, JA<->JBE, etc.
			return block;
		}

		// Normal relational comparisons: a <op> b
		if (!ctx.relOperator().isEmpty()) {
			String op = ctx.relOperator(0).getText();

			AsmAst leftAst = visit(ctx.arithOperation(0));
			AsmAst rightAst = visit(ctx.arithOperation(1));

			AsmBlock block = new AsmBlock(new ArrayList<>());
			emitValue(block, leftAst);
			emitValue(block, rightAst);

			block.addMember(COMPARE(op)); // emits JE/JNZ/JA/...
			return block;
		}

		return visitChildren(ctx);
	}

	@Override
	public AsmAst visitArithOperation(EightBitParser.ArithOperationContext ctx) {
		if (ctx.oper == null) {
			return visit(ctx.arithMonom().get(0));
		}

		List<TerminalNode> operatorNodes = Stream
				.concat(ctx.getTokens(EightBitParser.ADD).stream(), ctx.getTokens(EightBitParser.SUB).stream())
				.collect(Collectors.toList());
		operatorNodes.sort((t1, t2) -> Integer.compare(t1.getSymbol().getTokenIndex(), t2.getSymbol().getTokenIndex()));
		List<Character> operators = operatorNodes.stream().map(e -> e.getText().charAt(0)).collect(Collectors.toList());

		List<AsmAst> operands = ctx.arithMonom().stream().map(this::visit).collect(Collectors.toList());
		if (operands.isEmpty()) {
			return BLOCK();
		}

		AsmBlock block = new AsmBlock(new ArrayList<>());

		emitValue(block, operands.get(0));
		IntStream.range(0, operators.size()).forEach(i -> {
			emitValue(block, operands.get(i + 1));
			block.addMember(TWOOPERATION("A", "B", operators.get(i)));
		});

		return block;
	}

	@Override
	public AsmAst visitArithMonom(EightBitParser.ArithMonomContext ctx) {
		if (ctx.operTDArithSingle().isEmpty()) {
			return visitChildren(ctx);
		}

		AsmAst left = visit(ctx.arithSingle());
		List<AsmAst> rest = ctx.operTDArithSingle().stream().map(this::visit).collect(Collectors.toList());

		AsmBlock block = new AsmBlock(new ArrayList<>());

		// First operand
		emitValue(block, left);

		// For each (* or /): push RHS then apply op using stack convention [..., LHS,
		// RHS]
		rest.forEach(r -> {
			{
				AsmBlock opBlock = requireBlock(r, "monom op", ctx.getText());
				List<AsmAst> members = opBlock.getMembers();
				if (members.size() < 2) {
					throw new IllegalStateException("Invalid monom op block: " + ctx.getText());
				}

				AsmAst rhs = members.get(0);
				AsmID op = (AsmID) members.get(1);

				emitValue(block, rhs);
				block.addMember(ONEOPERATION("B", op.getValue()));
			}
		});

		return block;
	}

	@Override
	public AsmAst visitArithMinusSingle(EightBitParser.ArithMinusSingleContext ctx) {
		// Minimal, non-crashing unary minus support. If your backend has a dedicated
		// NEG op, use that instead.
		AsmAst inner = visit(ctx.arithOperation());
		if (inner instanceof AsmNum) {
			return NUM(-((AsmNum) inner).getValue());
		}

		// Fallback: generate (0 - inner) using existing binary op machinery.
		AsmBlock block = new AsmBlock(new ArrayList<>());
		block.addMember(PUSH(0));

		if (inner instanceof AsmID) {
			block.addMember(PUSH("[" + SymbolTable.getContext() + "_" + ((AsmID) inner).getValue() + "]"));
		} else if (inner instanceof AsmCall) {
			block.addMember(inner);
		} else if (inner instanceof AsmBlock) {
			block.getMembers().addAll(((AsmBlock) inner).getMembers());
		} else {
			throw new IllegalStateException("Unsupported unary minus operand: " + inner);
		}

		block.addMember(TWOOPERATION("A", "B", '-'));
		return block;
	}

	@Override
	public AsmAst visitArithParsSingle(EightBitParser.ArithParsSingleContext ctx) {
		// Parentheses should not lose the nested expression result (visitChildren()
		// would end with ')').
		return visit(ctx.expr());
	}

	@Override
	public AsmAst visitArithIdSingle(EightBitParser.ArithIdSingleContext ctx) {
		AsmID id = (AsmID) visit(ctx.id());
		if (ctx.arguments() != null) {
			// Calls used inside expressions must also advance call-context so string
			// literals
			// inside arguments are owned by the correct call and are pushed in the right
			// place.
			if ((id.getValue().equals("print_string")) && (!AsmFormatter.getPS())) {
				AsmFormatter.addCode(PRINTSTRING());
				AsmFormatter.setPS(true);
			}
			if ((id.getValue().equals("print_number")) && (!AsmFormatter.getPN())) {
				AsmFormatter.addCode(PRINTNUMBER());
				AsmFormatter.setPN(true);
			}
			if ((id.getValue().equals("print_boolean")) && (!AsmFormatter.getPB())) {
				if ((!AsmFormatter.getPS())) {
					AsmFormatter.addCode(PRINTSTRING());
					AsmFormatter.setPS(true);
				}
				AsmFormatter.addCode(PRINTBOOLEAN());
				AsmFormatter.setPB(true);
			}

			SymbolTable.setCallContext();

			AsmAst args = visit(ctx.arguments());
			return CALL(id, ARGS(args));
		} else {
			return visit(ctx.id());
		}
	}

	@Override
	public AsmAst visitArithConstantSingle(EightBitParser.ArithConstantSingleContext ctx) {
		return visitChildren(ctx);
	}

	@Override
	public AsmAst visitOperTDArithSingle(EightBitParser.OperTDArithSingleContext ctx) {
		return new AsmBlock(new ArrayList<>(Arrays.asList(visit(ctx.arithSingle()), ID(ctx.oper.getText()))));
	}

	@Override
	public AsmAst visitExprNum(EightBitParser.ExprNumContext ctx) {
		return NUM(parseU8Literal(ctx.NUMBER().getText(), ctx.getText()));
	}

	@Override
	public AsmAst visitExprString(EightBitParser.ExprStringContext ctx) {

		AsmFormatter.addData(STRING(ctx.getText()));

		return new AsmID(ctx.getText());
	}

	@Override
	public AsmAst visitExprTrue(EightBitParser.ExprTrueContext ctx) {
		return TRUE;
	}

	@Override
	public AsmAst visitExprFalse(EightBitParser.ExprFalseContext ctx) {
		return FALSE;
	}

	@Override
	public AsmAst visitExprNull(EightBitParser.ExprNullContext ctx) {
		AsmFormatter.addData(STRING(ctx.getText()));
		return new AsmID(ctx.getText());
	}

	@Override
	public AsmAst visitArguments(EightBitParser.ArgumentsContext ctx) {
		EightBitParser.ArgsContext args = ctx.args();
		return (args == null) ? BLOCK() : visit(args);
	}

	@Override
	public AsmAst visitArgs(EightBitParser.ArgsContext ctx) {
		return BLOCK(ctx.expr().stream().map(c -> visit(c)).collect(Collectors.toList()));
	}

	// ---------- Helper methods ----------

	private static int parseU8Literal(String raw, String where) {
		if (raw == null) {
			throw new IllegalArgumentException("Null numeric literal in: " + where);
		}
		String s = raw.trim();
		if (s.startsWith("-")) {
			throw new IllegalArgumentException(
					"Negative numeric literal not supported (0..255): " + s + " in: " + where);
		}
		if (s.contains(".")) {
			throw new IllegalArgumentException(
					"Decimal numeric literal not supported (0..255): " + s + " in: " + where);
		}
		int v;
		try {
			v = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid numeric literal: " + s + " in: " + where, e);
		}
		if (v < 0 || v > 255) {
			throw new IllegalArgumentException("Numeric literal out of range (0..255): " + v + " in: " + where);
		}
		return v;
	}

	private static Set<String> computeRecursiveContexts(Map<String, Set<String>> edges) {
		return edges.keySet()
				.stream()
				.filter(f -> reachesSelf(f, edges))
				.collect(java.util.stream.Collectors.toSet());
	}

	/**
	 * Ensures every string literal used on the RHS of a let-assignment is:
	 * 1) registered as a constant, and
	 * 2) emitted into the data area as: <context>_<label>: DB "<text>" \n DB 0
	 */
	private void ensureLetStringConstantsEmitted(EightBitParser.AssignStmtListContext listCtx) {
		String context = SymbolTable.getContext();
		Set<String> emitted = new HashSet<>();

		listCtx.assignStatement().forEach(aCtx -> {
			{
				String rhsText = aCtx.expr().getText();
				if (rhsText == null || !rhsText.contains("\"")) {
					return;
				}
				if (!emitted.add(rhsText)) {
					return;
				}

				String label = null;
				try {
					label = SymbolTable.findConstantByValue(context, rhsText);
				} catch (Exception ignored) {
					// some implementations throw when not found
				}

				if (label == null) {
					SymbolTable.addConstant(rhsText, -2);
					label = SymbolTable.findConstantByValue(context, rhsText);
					AsmFormatter.addData(ID(context + "_" + label + ": DB " + rhsText + "\nDB 0"));
				}
			}
		});
	}

	private AsmAst tryCompileSelfUpdate(AsmAst id, String rhsText) {
		String lhs = ((AsmID) id).getValue();

		// ANTLR getText() has no spaces. Match "x+1", "x-1", "x+y", "x-y".
		String qlhs = java.util.regex.Pattern.quote(lhs);
		java.util.regex.Pattern pNum = java.util.regex.Pattern.compile("^" + qlhs + "([+-])([0-9]+)$");
		java.util.regex.Pattern pVar = java.util.regex.Pattern.compile("^" + qlhs + "([+-])([A-Za-z][A-Za-z0-9_]*)$");

		java.util.regex.Matcher mNum = pNum.matcher(rhsText);
		java.util.regex.Matcher mVar = pVar.matcher(rhsText);

		String ctx = SymbolTable.getContext();
		String lhsAddr = "[" + ctx + "_" + lhs + "]";

		if (mNum.matches()) {
			String op = mNum.group(1);
			int imm = parseU8Literal(mNum.group(2), rhsText);

			AsmBlock pre = new AsmBlock(new ArrayList<>());
			pre.addMember(new AsmTwoAddress("A", lhsAddr, "MOV"));
			pre.addMember(new AsmTwoAddress("A", imm, op.equals("+") ? "ADD" : "SUB"));
			return ASSIGN_NUM(id, "A", pre, "");
		}

		if (mVar.matches()) {
			String op = mVar.group(1);
			String rhsVar = mVar.group(2);
			String rhsAddr = "[" + ctx + "_" + rhsVar + "]";

			AsmBlock pre = new AsmBlock(new ArrayList<>());
			pre.addMember(new AsmTwoAddress("A", lhsAddr, "MOV"));
			pre.addMember(new AsmTwoAddress("A", rhsAddr, op.equals("+") ? "ADD" : "SUB"));
			return ASSIGN_NUM(id, "A", pre, "");
		}

		return null;
	}

	private static void maybeInvertConditionJump(String exprText, AsmAst cmp) {
		if (exprText == null || exprText.isEmpty())
			return;

		int bangs = (int) exprText.chars().takeWhile(c -> c == '!').count();
		if (bangs % 2 == 0)
			return;

		if (!(cmp instanceof AsmID)) {
			throw new IllegalStateException("Expected AsmID compare-jump for negated condition, got: "
					+ (cmp == null ? "null" : cmp.getClass().getSimpleName()));
		}

		AsmID j = (AsmID) cmp;
		j.setValue(invertCompareJump(j.getValue()));
	}

	private AsmBlock requireBlock(AsmAst ast, String where, String source) {
		if (ast == null) {
			throw new IllegalStateException("Null codegen in " + where + ": " + source);
		}
		if (!(ast instanceof AsmBlock)) {
			throw new IllegalStateException(
					"Expected AsmBlock in " + where + " but got " + ast.getClass().getSimpleName() + ": " + source);
		}
		return (AsmBlock) ast;
	}

	private AsmAst tryCompileBoolReturnAsIf(EightBitParser.ExprContext exprCtx) {
		// Supports: <arithOp> (==|!=) <arithOp> as a return expression.
		if (exprCtx.relMonom().size() != 1) {
			return null;
		}
		EightBitParser.RelMonomContext monom = exprCtx.relMonom(0);
		if (monom.relOperation().size() != 1) {
			return null;
		}
		EightBitParser.RelOperationContext rel = monom.relOperation(0);
		if (rel.relOperator().size() != 1 || rel.arithOperation().size() != 2) {
			return null;
		}

		String op = rel.relOperator(0).getText();
		if (!"==".equals(op) && !"!=".equals(op)) {
			return null;
		}

		AsmAst left = visit(rel.arithOperation(0));
		AsmAst right = visit(rel.arithOperation(1));

		AsmBlock cond = new AsmBlock(new ArrayList<>());
		emitValue(cond, left);
		emitValue(cond, right);

		AsmAst cmp = COMPARE(op);
		cond.addMember(cmp);

		// IF expects the compare node separated from the expression block.
		cond.getMembers().remove(cond.getMembers().size() - 1);

		AsmAst accept = RETURN(NUM(1));
		AsmAst reject = RETURN(NUM(0));
		return IF(cond, cmp, accept, reject);
	}

	private String invertJump(String jmp) {
		if (jmp == null)
			return null;
		String t = jmp.replace("\t", "").trim();

		switch (t) {
			case "JE":
			case "JZ":
				return "\tJNZ";
			case "JNZ":
			case "JNE":
				return "\tJE";
			case "JA":
				return "\tJBE";
			case "JAE":
				return "\tJB";
			case "JB":
				return "\tJAE";
			case "JBE":
				return "\tJA";
			default:
				return jmp;
		}
	}

	private void emitValue(AsmBlock out, AsmAst value) {
		if (value == null) {
			return;
		}
		if (value instanceof AsmBlock) {
			out.getMembers().addAll(((AsmBlock) value).getMembers());
			return;
		}
		if (value instanceof AsmNum) {
			out.addMember(PUSH(((AsmNum) value).getValue()));
			return;
		}
		if (value instanceof AsmID) {
			String name = ((AsmID) value).getValue();
			out.addMember(PUSH("[" + SymbolTable.getContext() + "_" + name + "]"));
			return;
		}
		if (value instanceof AsmCall) {
			out.addMember(value);
			return;
		}
		// Fallback: try visiting children already produced raw asm nodes.
		out.addMember(value);
	}

	/**
	 * Inverts a conditional-jump mnemonic used by
	 * {@link AsmEmitter#COMPARE(String)}.
	 * Needed because while/if/for extract the compare-jump node and attach targets
	 * later.
	 */
	private static String invertCompareJump(String raw) {
		String s = Optional.ofNullable(raw).orElse("");
		int firstNonWs = IntStream.range(0, s.length())
				.filter(i -> !Character.isWhitespace(s.charAt(i)))
				.findFirst()
				.orElse(s.length());

		String prefix = s.substring(0, firstNonWs);
		String op = s.substring(firstNonWs).trim();

		String inv = switch (op) {
			case "JE" -> "JNZ";
			case "JNZ" -> "JE";
			case "JA" -> "JBE";
			case "JBE" -> "JA";
			case "JAE" -> "JB";
			case "JB" -> "JAE";
			case "JC" -> "JNC";
			case "JNC" -> "JC";
			default -> throw new IllegalArgumentException("Cannot invert conditional jump: '" + raw + "'");
		};

		return prefix + inv;
	}

	private static boolean reachesSelf(String start, Map<String, Set<String>> edges) {
		// direct self-edge counts as recursion too
		if (edges.getOrDefault(start, Set.of()).contains(start))
			return true;

		record State(List<String> frontier, Set<String> seen) {
		}

		UnaryOperator<State> step = st -> {
			List<String> nextFrontier = st.frontier().stream()
					.flatMap(cur -> edges.getOrDefault(cur, Set.of()).stream())
					.filter(nxt -> !st.seen().contains(nxt))
					.toList();

			Set<String> nextSeen = Stream.concat(st.seen().stream(), nextFrontier.stream())
					.collect(Collectors.toUnmodifiableSet());

			return new State(nextFrontier, nextSeen);
		};

		State seed = new State(List.of(start), Set.of(start));

		return Stream.iterate(seed, st -> !st.frontier().isEmpty(), step)
				.anyMatch(st -> st.frontier().stream()
						.anyMatch(cur -> !cur.equals(start)
								&& edges.getOrDefault(cur, Set.of()).contains(start)));
	}

	// ---------- Helper Classes ----------
	private static final class CallGraphVisitor extends EightBitBaseVisitor<Void> {
		private final Map<String, Set<String>> edges;
		private final Set<String> fnNames;
		private String currentFn = null;

		CallGraphVisitor(Map<String, Set<String>> edges, Set<String> fnNames) {
			this.edges = edges;
			this.fnNames = fnNames;
		}

		@Override
		public Void visitEightFunction(EightBitParser.EightFunctionContext ctx) {
			currentFn = ctx.id().getText();
			return visitChildren(ctx);
		}

		@Override
		public Void visitCallStatement(EightBitParser.CallStatementContext ctx) {
			if (currentFn != null) {
				String callee = ctx.ID().getText();
				if (fnNames.contains(callee)) {
					edges.get(currentFn).add(callee);
				}
			}
			return visitChildren(ctx);
		}

		@Override
		public Void visitArithIdSingle(EightBitParser.ArithIdSingleContext ctx) {
			if (currentFn != null && ctx.arguments() != null) {
				String callee = ctx.id().getText();
				if (fnNames.contains(callee)) {
					edges.get(currentFn).add(callee);
				}
			}
			return visitChildren(ctx);
		}
	}

}
