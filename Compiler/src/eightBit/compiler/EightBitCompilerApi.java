package eightBit.compiler;

import eightBit.antlr.EightBitLexer;
import eightBit.antlr.EightBitParser;
import eightBit.registry.AsmFormatter;
import eightBit.registry.SymbolTable;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class EightBitCompilerApi {

    public CompilationResult compileToAsm(String sourceCode) {
        if (sourceCode == null) {
            return new CompilationResult(
                    CompilationResult.Status.COMPILE_ERROR,
                    "",
                    List.of(new Diagnostic(Diagnostic.Severity.ERROR, null, null, "sourceCode is null")));
        }

        try {
            CharStream input = CharStreams.fromString(sourceCode);

            EightBitLexer lexer = new EightBitLexer(input);
            CollectingErrorListener errors = new CollectingErrorListener();
            lexer.removeErrorListeners();
            lexer.addErrorListener(errors);

            CommonTokenStream tokens = new CommonTokenStream(lexer);

            EightBitParser parser = new EightBitParser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(errors);

            ParseTree tree = parser.eightProgram();

            if (errors.hasErrors()) {
                return new CompilationResult(CompilationResult.Status.COMPILE_ERROR, "", errors.getDiagnostics());
            }

            Compiler compiler = new Compiler();
            compiler.compile(tree);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (PrintStream out = new PrintStream(baos, true, StandardCharsets.UTF_8)) {
                compiler.genCode(out);
            }

            return new CompilationResult(CompilationResult.Status.SUCCESS, baos.toString(StandardCharsets.UTF_8),
                    List.of());

        } catch (CompilationException e) {
            Diagnostic d = new Diagnostic(
                    Diagnostic.Severity.ERROR,
                    e.getLine(),
                    e.getColumn(),
                    e.getMessage());
            return new CompilationResult(CompilationResult.Status.COMPILE_ERROR, "", List.of(d));

        } catch (IllegalArgumentException | IllegalStateException e) {
            // Treat as compiler internal/semantic error; location may be unknown.
            String msg = (e.getMessage() == null) ? e.getClass().getSimpleName() : e.getMessage();
            Diagnostic d = new Diagnostic(Diagnostic.Severity.ERROR, null, null, msg);
            return new CompilationResult(CompilationResult.Status.COMPILE_ERROR, "", List.of(d));

        } catch (Exception e) {
            String msg = (e.getMessage() == null) ? e.getClass().getSimpleName() : e.getMessage();
            Diagnostic d = new Diagnostic(Diagnostic.Severity.ERROR, null, null, msg);
            return new CompilationResult(CompilationResult.Status.COMPILE_ERROR, "", List.of(d));

        } finally {
            SymbolTable.clearContext();
            AsmFormatter.clearContext();
        }
    }

    private static final class CollectingErrorListener extends BaseErrorListener {
        private final List<Diagnostic> diagnostics = new ArrayList<>();

        @Override
        public void syntaxError(
                Recognizer<?, ?> recognizer,
                Object offendingSymbol,
                int line,
                int charPositionInLine,
                String msg,
                RecognitionException e) {
            diagnostics.add(new Diagnostic(Diagnostic.Severity.ERROR, line, charPositionInLine, msg));
        }

        boolean hasErrors() {
            return !diagnostics.isEmpty();
        }

        List<Diagnostic> getDiagnostics() {
            return List.copyOf(diagnostics);
        }
    }

}
