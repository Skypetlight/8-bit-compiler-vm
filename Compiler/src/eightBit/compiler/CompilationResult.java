package eightBit.compiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class CompilationResult {

    public enum Status {
        SUCCESS,
        COMPILE_ERROR
    }

    private final Status status;
    private final String asm;
    private final List<Diagnostic> diagnostics;

    /**
     * Backwards-compatible constructor: maps a plain error string into a single
     * ERROR diagnostic.
     */
    public CompilationResult(Status status, String asm, String error) {
        this(status, asm, toDiagnostics(error));
    }

    public CompilationResult(Status status, String asm, List<Diagnostic> diagnostics) {
        this.status = Objects.requireNonNull(status, "status");
        this.asm = asm == null ? "" : asm;
        List<Diagnostic> copy = new ArrayList<>(
                diagnostics == null ? Collections.<Diagnostic>emptyList() : diagnostics);
        this.diagnostics = Collections.unmodifiableList(copy);
    }

    public Status getStatus() {
        return status;
    }

    public String getAsm() {
        return asm;
    }

    public List<Diagnostic> getDiagnostics() {
        return diagnostics;
    }

    /**
     * Convenience for legacy callers: formatted diagnostics joined with newlines.
     */
    public String getError() {
        if (diagnostics.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < diagnostics.size(); i++) {
            if (i > 0)
                sb.append('\n');
            sb.append(diagnostics.get(i).format());
        }
        return sb.toString();
    }

    private static List<Diagnostic> toDiagnostics(String error) {
        if (error == null) {
            return Collections.emptyList();
        }
        String trimmed = error.trim();
        if (trimmed.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new Diagnostic(Diagnostic.Severity.ERROR, null, null, trimmed));
    }
}
