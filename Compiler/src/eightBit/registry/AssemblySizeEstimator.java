package eightBit.registry;

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Rough byte-size estimator for the target VM.
 *
 * VM rules:
 * - every instruction is 1 byte
 * - every operand is 1 byte
 * - DB emits one byte per element; DB "str" emits str.length bytes (quotes not
 * included)
 */
public final class AssemblySizeEstimator {

    private static final Pattern LABEL_ONLY = Pattern.compile("^\\s*[A-Za-z_.][A-Za-z0-9_.]*:\\s*$");
    private static final Pattern LABEL_PREFIX = Pattern.compile("^\\s*([A-Za-z_.][A-Za-z0-9_.]*:)\\s*(.*)$");
    private static final Pattern DB_LINE = Pattern.compile("^\\s*DB\\s+(.+?)\\s*$");

    private AssemblySizeEstimator() {
    }

    public static int estimateBytes(String asm) {
        return Arrays.stream(asm.split("\\R"))
                .map(AssemblySizeEstimator::stripComment)
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .map(AssemblySizeEstimator::extractContentAfterLabel)
                .flatMap(Optional::stream)
                .mapToInt(AssemblySizeEstimator::estimateContentBytes)
                .sum();
    }

    private static Optional<String> extractContentAfterLabel(String line) {
        if (LABEL_ONLY.matcher(line).matches()) {
            return Optional.empty();
        }

        Matcher labelPref = LABEL_PREFIX.matcher(line);
        if (!labelPref.matches()) {
            return Optional.of(line);
        }

        String content = labelPref.group(2).trim();
        return content.isEmpty() ? Optional.empty() : Optional.of(content);
    }

    private static int estimateContentBytes(String content) {
        Matcher mDb = DB_LINE.matcher(content);
        if (mDb.matches()) {
            return estimateDbPayloadBytes(mDb.group(1).trim());
        }
        return estimateInstructionBytes(content);
    }

    private static String stripComment(String line) {
        int idx = line.indexOf(';');
        return (idx >= 0) ? line.substring(0, idx) : line;
    }

    private static int estimateDbPayloadBytes(String payload) {
        // Only strings or single constants are emitted by this compiler.
        if (payload.startsWith("\"") && payload.endsWith("\"") && payload.length() >= 2) {
            String inner = payload.substring(1, payload.length() - 1);
            inner = inner.replace("\\\"", "\""); // minimal unescape for size
            return inner.length();
        }
        return 1;
    }

    private static int estimateInstructionBytes(String content) {
        String[] parts = content.trim().split("\\s+", 2);
        String operands = (parts.length > 1) ? parts[1].trim() : "";

        long nOps = operands.isEmpty()
                ? 0
                : 1 + operands.chars().filter(ch -> ch == ',').count();

        return 1 + (int) nOps;
    }

}