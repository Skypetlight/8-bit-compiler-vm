package eightBit.compiler;

import java.util.Objects;

/**
 * A structured diagnostic message produced during lexing/parsing/compilation.
 *
 * <p>
 * Line/column are 1-based for line and 0-based for column, matching ANTLR's
 * default reporting.
 * </p>
 */
public final class Diagnostic {

    public enum Severity {
        ERROR,
        WARNING
    }

    private final Severity severity;
    private final Integer line;
    private final Integer column;
    private final String message;

    public Diagnostic(Severity severity, Integer line, Integer column, String message) {
        this.severity = Objects.requireNonNull(severity, "severity");
        this.line = line;
        this.column = column;
        this.message = Objects.requireNonNull(message, "message");
    }

    public Severity getSeverity() {
        return severity;
    }

    public Integer getLine() {
        return line;
    }

    public Integer getColumn() {
        return column;
    }

    public String getMessage() {
        return message;
    }

    public String format() {
        if (line == null || column == null) {
            return severity + ": " + message;
        }
        return severity + " line " + line + ":" + column + " " + message;
    }
}
