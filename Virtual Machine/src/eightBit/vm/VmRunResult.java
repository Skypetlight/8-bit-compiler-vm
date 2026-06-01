package eightBit.vm;

public class VmRunResult {

    public enum Status {
        SUCCESS,
        ASSEMBLE_ERROR,
        RUNTIME_ERROR
    }

    private final Status status;
    private final String stdout;
    private final String stderr;
    private final long durationMs;

    public VmRunResult(Status status, String stdout, String stderr, long durationMs) {
        this.status = status;
        this.stdout = stdout;
        this.stderr = stderr;
        this.durationMs = durationMs;
    }

    public Status getStatus() {
        return status;
    }

    public String getStdout() {
        return stdout;
    }

    public String getStderr() {
        return stderr;
    }

    public long getDurationMs() {
        return durationMs;
    }

}
