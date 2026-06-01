package eightBit.compiler;

public class CompilationException extends RuntimeException {

    private final Integer line; // 1-based (ANTLR)
    private final Integer column; // 0-based (ANTLR)

    public CompilationException(String message) {
        super(message);
        this.line = null;
        this.column = null;
    }

    public CompilationException(String message, Throwable cause) {
        super(message, cause);
        this.line = null;
        this.column = null;
    }

    public CompilationException(String message, Integer line, Integer column) {
        super(message);
        this.line = line;
        this.column = column;
    }

    public Integer getLine() {
        return line;
    }

    public Integer getColumn() {
        return column;
    }

}