package eightBit.compiler;

import java.io.FileInputStream;
import java.io.InputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import eightBit.antlr.EightBitLexer;
import eightBit.antlr.EightBitParser;

public class EightBitc {

	public static void main(String[] args) throws Exception {
		String inputFile = null;
		if (args.length > 0) {
			inputFile = args[0];
		}

		InputStream is = System.in;

		if (inputFile != null) {
			is = new FileInputStream(inputFile);
			System.err.println("EightBitc Reading from " + inputFile);
		} else {
			System.err.println("EightBitc Reading from console (enter CTRL-Z+ENTER to finish)");
		}

		CharStream input = CharStreams.fromStream(is);
		EightBitLexer lexer = new EightBitLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		EightBitParser parser = new EightBitParser(tokens);
		ParseTree tree = parser.eightProgram();

		System.err.println("EightBitc v1.0 CR II-2016");
		Compiler comp = new Compiler();
		comp.compile(tree);
		comp.genCode();

	}

}
