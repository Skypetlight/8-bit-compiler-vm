/**
 * Peephole optimizer for the 8-bit assembly output.
 *
 * This compiler emits verbose stack-based code. The VM has only 256 bytes total,
 * so small reductions matter. The optimizer applies safe local rewrites such as:
 * - PUSH x; POP R    -> MOV R, x
 * - PUSH x; PUSH y; POP B; POP A; OP A,B; PUSH A  -> MOV A,x; OP A,y; PUSH A
 * - PUSH x; PUSH y; POP B; POP A; CMP A,B         -> MOV A,x; MOV B,y; CMP A,B
 *
 * Only transforms contiguous instruction runs; labels/DB lines are preserved.
 */
package eightBit.registry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class PeepholeOptimizer {

    private static final Pattern PUSH = Pattern.compile("^\\s*PUSH\\s+(.+?)\\s*$");
    private static final Pattern POP = Pattern.compile("^\\s*POP\\s+([A-D]|SP)\\s*$");
    private static final Pattern POP_A = Pattern.compile("^\\s*POP\\s+A\\s*$");
    private static final Pattern POP_B = Pattern.compile("^\\s*POP\\s+B\\s*$");

    private PeepholeOptimizer() {
    }

    public static String optimize(String asm) {
        List<String> lines = splitLinesPreserve(asm);
        List<String> out = new ArrayList<>(lines.size());

        int i = 0;
        while (i < lines.size()) {
            String l0 = lines.get(i);

            if (isBarrier(l0)) {
                out.add(l0);
                i++;
                continue;
            }

            // Rule 1: PUSH x; POP r => MOV r, x (or remove if r==x)
            if (i + 1 < lines.size() && !isBarrier(lines.get(i + 1))) {
                Matcher mPush = PUSH.matcher(l0);
                Matcher mPop = POP.matcher(lines.get(i + 1));
                if (mPush.matches() && mPop.matches()) {
                    String x = mPush.group(1).trim();
                    String r = mPop.group(1).trim();
                    if (x.equals(r)) {
                        i += 2;
                        continue;
                    }
                    out.add("\tMOV " + r + ", " + x);
                    i += 2;
                    continue;
                }
            }

            // Rule 2: PUSH x; PUSH y; POP B; POP A; CMP A, B => MOV A,x; MOV B,y; CMP A,B
            if (i + 4 < lines.size()
                    && matchPush(lines.get(i))
                    && matchPush(lines.get(i + 1))
                    && POP_B.matcher(lines.get(i + 2)).matches()
                    && POP_A.matcher(lines.get(i + 3)).matches()
                    && lines.get(i + 4).trim().equals("CMP A, B")) {

                String x = extractPushOperand(lines.get(i));
                String y = extractPushOperand(lines.get(i + 1));
                if (!isRegisterOperand(x) && !isRegisterOperand(y)) {
                    out.add("\tMOV A, " + x);
                    out.add("\tMOV B, " + y);
                    out.add("\tCMP A, B");
                    i += 5;
                    continue;
                }
            }

            // Rule 3: PUSH x; PUSH y; POP B; POP A; <OP> A, B; PUSH A => MOV A,x; <OP> A,
            // y; PUSH A
            if (i + 5 < lines.size()
                    && matchPush(lines.get(i))
                    && matchPush(lines.get(i + 1))
                    && POP_B.matcher(lines.get(i + 2)).matches()
                    && POP_A.matcher(lines.get(i + 3)).matches()
                    && lines.get(i + 5).trim().equals("PUSH A")) {

                String opLine = lines.get(i + 4).trim();
                if (opLine.equals("ADD A, B") || opLine.equals("SUB A, B") || opLine.equals("MUL B")
                        || opLine.equals("DIV B")) {
                    String x = extractPushOperand(lines.get(i));
                    String y = extractPushOperand(lines.get(i + 1));
                    if (!isRegisterOperand(x) && !isRegisterOperand(y)) {
                        out.add("\tMOV A, " + x);

                        if (opLine.equals("ADD A, B"))
                            out.add("\tADD A, " + y);
                        else if (opLine.equals("SUB A, B"))
                            out.add("\tSUB A, " + y);
                        else if (opLine.equals("MUL B"))
                            out.add("\tMUL " + y);
                        else
                            out.add("\tDIV " + y);

                        out.add("\tPUSH A");
                        i += 6;
                        continue;
                    }
                }
            }

            // Default: keep
            out.add(l0);
            i++;
        }

        return String.join("\n", out);
    }

    private static boolean isRegisterOperand(String op) {
        String t = op.trim();
        return t.equals("A") || t.equals("B") || t.equals("C") || t.equals("D") || t.equals("SP");
    }

    private static boolean matchPush(String line) {
        return PUSH.matcher(line).matches();
    }

    private static String extractPushOperand(String line) {
        Matcher m = PUSH.matcher(line);
        if (!m.matches())
            return "";
        return m.group(1).trim();
    }

    private static boolean isBarrier(String line) {
        String t = line.trim();
        if (t.isEmpty())
            return true;
        if (t.endsWith(":"))
            return true;
        // Keep DBs untouched (data section and strings)
        if (t.startsWith("DB ") || t.contains(": DB"))
            return true;
        // section headers
        if (t.equals("start:") || t.equals("data_area:") || t.equals("code_area:") || t.equals("main_area:"))
            return true;
        return false;
    }

    private static List<String> splitLinesPreserve(String s) {
        String[] arr = s.split("\\r?\\n", -1);
        int lastNonEmpty = IntStream.range(0, arr.length)
                .filter(i -> !arr[i].isEmpty())
                .reduce((a, b) -> b)
                .orElse(-1);

        return Arrays.stream(arr)
                .limit(lastNonEmpty + 1L)
                .collect(Collectors.toList());
    }
}
