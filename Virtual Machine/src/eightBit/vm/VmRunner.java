package eightBit.vm;

public final class VmRunner {

    private static final int CONSOLE_START = 232;

    private VmRunner() {
    }

    public static VmRunResult runAsm(String asmSource) {
        long startNs = System.nanoTime();
        try {
            CompileResult assembled = new Asm().go(asmSource);
            Memory memory = new Memory();
            memory.loadProgram(assembled.getCode());

            Cpu cpu = new Cpu(memory);
            cpu.runProgram();

            String out = readConsole(memory);
            long ms = (System.nanoTime() - startNs) / 1_000_000L;
            return new VmRunResult(VmRunResult.Status.SUCCESS, out, "", ms);
        } catch (ParseException e) {
            long ms = (System.nanoTime() - startNs) / 1_000_000L;
            return new VmRunResult(VmRunResult.Status.ASSEMBLE_ERROR, "", e.getMessage(), ms);
        } catch (Exception e) {
            long ms = (System.nanoTime() - startNs) / 1_000_000L;
            String msg = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            return new VmRunResult(VmRunResult.Status.RUNTIME_ERROR, "", msg, ms);
        }
    }

    private static String readConsole(Memory memory) {
        StringBuilder sb = new StringBuilder();
        for (int ptr = CONSOLE_START; memory.load(ptr) != 0; ptr++) {
            sb.append((char) memory.load(ptr));
        }
        return sb.toString();
    }
}