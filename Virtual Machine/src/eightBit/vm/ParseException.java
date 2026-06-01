package eightBit.vm;

public class ParseException extends RuntimeException {
    
    private final int line;
    private final String reason;

    public ParseException(int line, String reason) {
        super("Line " + line + ": " + reason);
        this.line = line;
        this.reason = reason;
    }

    public int getLine() {
        return line;
    }

    public String getReason() {
        return reason;
    }

}