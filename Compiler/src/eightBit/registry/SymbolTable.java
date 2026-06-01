package eightBit.registry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SymbolTable {

    private static final ThreadLocal<SymbolTable> CTX = ThreadLocal.withInitial(SymbolTable::new);

    private int ifNumber;
    private int whileNumber;
    private int forNumber;
    private String context;
    private int callContext;
    private HashMap<String, Function> functions;
    private Set<String> framePreserveContexts;

    private SymbolTable() {
        resetInstance();
    }

    public static void reset() {
        CTX.get().resetInstance();
    }

    private void resetInstance() {
        context = ".program";
        ifNumber = 0;
        whileNumber = 0;
        forNumber = 0;
        callContext = -1;
        functions = new HashMap<>();
        functions.put(context, new Function());
        framePreserveContexts = new HashSet<>();
    }

    private static SymbolTable ctx() {
        return CTX.get();
    }

    // Set & get methods
    public static void setContext(String ctx) {
        SymbolTable st = ctx();
        st.context = "." + ctx;
        st.addFunction();
    }

    public static String getContext() {
        return ctx().context;
    }

    public static void setCallContext() {
        ctx().callContext++;
    }

    public static int getCallContext() {
        return ctx().callContext;
    }

    public static int getIfNumber() {
        SymbolTable st = ctx();
        return st.ifNumber++;
    }

    public static int getWhileNumber() {
        SymbolTable st = ctx();
        return st.whileNumber++;
    }

    public static int getForNumber() {
        SymbolTable st = ctx();
        return st.forNumber++;
    }
    // End of set & get methods

    // Add methods
    private void addFunction() {
        functions.put(context, new Function());
    }

    public static String addConstant(Object v, int o) {
        SymbolTable st = ctx();
        return st.findFunction(st.context).addConstant(v, o);
    }

    public static void addVariable(String n, Object v, int o) {
        SymbolTable st = ctx();
        st.findFunction(st.context).addVariable(n, v, o);
    }
    // End of add methods

    // Find methods
    private Function findFunction(String c) {
        return functions.get(c);
    }

    public static Value findConstant(String f, String n) {
        return ctx().findFunction(f).findConstant(n);
    }

    public static Value findVariable(String f, String n) {
        return ctx().findFunction(f).findVariable(n);
    }

    public static String findConstantByValue(String f, String v) {
        return ctx().findFunction(f).findConstantByValue(v);
    }

    public static String findVariableByValue(String f, Object v) {
        return ctx().findFunction(f).findVariableByValue(v);
    }
    // End of find methods

    // Get methods
    public static List<String> getConstantsToUse(String f, int n) {
        return ctx().findFunction(f).getConstantsToUse(n);
    }

    public static List<String> getVariablesToUse(String f, int n) {
        return ctx().findFunction(f).getVariablesToUse(n);
    }
    // End of get methods

    // Other methods
    public static boolean isNumber(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
    }

    /**
     * Contexts are stored with the dot prefix, e.g. ".factorial".
     */
    public static void setFramePreserveContexts(Set<String> contexts) {
        SymbolTable st = ctx();
        st.framePreserveContexts = (contexts == null) ? new HashSet<>() : new HashSet<>(contexts);
    }

    public static boolean mustPreserveFrame(String ctx) {
        return ctx().framePreserveContexts.contains(ctx);
    }

    public static void clearContext() {
        CTX.remove();
    }
    // End of other methods

}
