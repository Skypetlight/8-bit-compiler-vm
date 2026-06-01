package eightBit.compiler;

import eightBit.asm.AsmAssign;
import eightBit.asm.AsmAst;
import eightBit.asm.AsmBlock;
import eightBit.asm.AsmBool;
import eightBit.asm.AsmCall;
import eightBit.asm.AsmEmpty;
import eightBit.asm.AsmFor;
import eightBit.asm.AsmFunction;
import eightBit.asm.AsmID;
import eightBit.asm.AsmIf;
import eightBit.asm.AsmLet;
import eightBit.asm.AsmMain;
import eightBit.asm.AsmModulo;
import eightBit.asm.AsmNum;
import eightBit.asm.AsmOneAddress;
import eightBit.asm.AsmOneOperation;
import eightBit.asm.AsmPrintBoolean;
import eightBit.asm.AsmPrintNumber;
import eightBit.asm.AsmPrintString;
import eightBit.asm.AsmProgram;
import eightBit.asm.AsmReturn;
import eightBit.asm.AsmString;
import eightBit.asm.AsmTwoOperation;
import eightBit.asm.AsmWhile;
import eightBit.registry.AsmFormatter;
import java.util.Arrays;
import java.util.List;

public interface AsmEmitter {

	final AsmBool TRUE = new AsmBool(true);
	final AsmBool FALSE = new AsmBool(false);

	default AsmProgram PROGRAM(List<AsmAst> code) {
		return new AsmProgram(code);
	}

	default AsmFunction FUNCTION(AsmID id, List<AsmAst> prolog, AsmAst body) {
		return new AsmFunction(id, prolog, body);
	}

	default AsmMain MAIN(AsmID id, AsmAst body) {
		return new AsmMain(id, body);
	}

	default List<AsmAst> PROLOGUE(AsmAst... args) {
		return Arrays.asList(args);
	}

	default AsmBlock BLOCK(List<AsmAst> members) {
		return new AsmBlock(members);
	}

	default AsmBlock BLOCK() {
		return new AsmBlock(Arrays.asList());
	}

	default AsmEmpty EMPTY() {
		return new AsmEmpty();
	}

	default AsmAssign ASSIGN_STRING(AsmAst name, String arg, String c) {
		return new AsmAssign(ID(""), ((AsmID) name).getValue(), arg, c);
	}

	default AsmAssign ASSIGN_NUM(AsmAst name, String arg, AsmAst pre, String c) {
		return new AsmAssign(pre, ((AsmID) name).getValue(), arg, c);
	}

	default AsmBlock ASSIGNLIST(List<AsmAst> as) {
		return new AsmBlock(as);
	}

	default AsmLet LET(AsmAst l, AsmAst c) {
		return new AsmLet(l, c);
	}

	default AsmFor FOR(AsmAst p, AsmAst i, AsmAst c, AsmAst l, AsmAst b) {
		return new AsmFor(p, i, c, l, b);
	}

	default AsmWhile WHILE(AsmAst p, AsmAst c, AsmAst b) {
		return new AsmWhile(p, c, b);
	}

	default List<AsmAst> ARGS(AsmAst... args) {
		return Arrays.asList(args);
	}

	default AsmID ID(String value) {
		return new AsmID(value);
	}

	default AsmReturn RETURN(AsmAst e) {
		return new AsmReturn(e);
	}

	default AsmCall CALL(AsmAst f, List<AsmAst> args) {
		return new AsmCall(f, args);
	}

	default AsmAst ONEOPERATION(Object arg, String oper) {
		switch (oper) {
			case "*":
				return new AsmOneOperation(arg, "MUL");
			case "/":
				return new AsmOneOperation(arg, "DIV");
			case "%":
				return new AsmModulo();
		}
		return null;
	}

	default AsmAst TWOOPERATION(String left, Object right, char oper) {
		switch (oper) {
			case '+':
				return new AsmTwoOperation(left, right, "ADD");
			case '-':
				return new AsmTwoOperation(left, right, "SUB");
		}
		return null;
	}

	default AsmID COMPARE(String cmp) {
		switch (cmp) {
			case ">":
				return new AsmID("\tJA");

			case ">=":
				return new AsmID("\tJAE");

			case "<":
				return new AsmID("\tJB");

			case "<=":
				return new AsmID("\tJBE");

			case "==":
				return new AsmID("\tJE");

			case "!=":
				return new AsmID("\tJNZ");
		}
		return null;
	}

	default AsmIf IF(AsmAst p, AsmAst c, AsmAst a, AsmAst r) {
		return new AsmIf(p, c, a, r);
	}

	default AsmOneAddress PUSH(Object o) {
		return new AsmOneAddress(o, "PUSH");
	}

	default AsmOneAddress POP(String a) {
		return new AsmOneAddress(a, "POP");
	}

	default AsmString STRING(String value) {
		return new AsmString(value);
	}

	default AsmNum NUM(int value) {
		return new AsmNum(value);
	}

	default AsmPrintString PRINTSTRING() {
		return new AsmPrintString();
	}

	default AsmPrintNumber PRINTNUMBER() {
		return new AsmPrintNumber();
	}

	default AsmPrintBoolean PRINTBOOLEAN() {
		AsmFormatter.addData(new AsmID(".true: DB \"true \"\nDB 0"));
		AsmFormatter.addData(new AsmID(".false: DB \"false \"\nDB 0"));
		return new AsmPrintBoolean();
	}

}
