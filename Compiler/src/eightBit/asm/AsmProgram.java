package eightBit.asm;

import eightBit.registry.AsmFormatter;
import eightBit.registry.AssemblySizeEstimator;
import eightBit.registry.PeepholeOptimizer;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

public class AsmProgram implements AsmAst {

    private static final int VM_MEMORY_BYTES = 256;
    private static final int STACK_TOP = 231; // VM stack starts here and grows down

    /**
     * Override with -Deightbit.stackHeadroom=8 (or any int) for extra safety.
     */
    private static final int DEFAULT_STACK_HEADROOM = 3;

    private List<AsmAst> code;

    public AsmProgram(List<AsmAst> c) {
        this.code = c;
    }

    public List<AsmAst> getCode() {
        return code;
    }

    public void setCode(List<AsmAst> code) {
        this.code = code;
    }

    private static int readHeadroom() {
        String prop = System.getProperty("eightbit.stackHeadroom");
        if (prop == null || prop.isBlank())
            return DEFAULT_STACK_HEADROOM;
        try {
            int v = Integer.parseInt(prop.trim());
            if (v < 0)
                throw new IllegalArgumentException();
            return v;
        } catch (Exception e) {
            throw new IllegalStateException("Invalid -Deightbit.stackHeadroom=" + prop + " (must be >= 0)");
        }
    }

    @Override
    public void genCode(PrintStream out) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream tmp = new PrintStream(baos);

        tmp.println("start:");
        tmp.println("\tJMP .main");

        // Put *all executable code* early (low addresses)
        tmp.println("code_area:");
        AsmFormatter.genCode(tmp);

        tmp.println("main_area:");
        AsmFormatter.genMain(tmp);

        // Put *data* last (high addresses)
        tmp.println("data_area:");
        AsmFormatter.genData(tmp);

        tmp.flush();

        String optimized = PeepholeOptimizer.optimize(baos.toString());

        int bytes = AssemblySizeEstimator.estimateBytes(optimized);
        if (bytes > VM_MEMORY_BYTES) {
            throw new IllegalStateException("Program too large for VM memory: " + bytes + " > " + VM_MEMORY_BYTES);
        }

        int headroom = readHeadroom();
        int safeLimit = (STACK_TOP + 1) - headroom;
        if (bytes > safeLimit) {
            throw new IllegalStateException(
                    "Program too close to stack/console region: " + bytes + " bytes. " +
                            "Safe limit is " + safeLimit + " bytes (STACK_TOP=" + STACK_TOP +
                            ", stackHeadroom=" + headroom + ").");
        }

        out.print(optimized);
        out.println();
    }

}
