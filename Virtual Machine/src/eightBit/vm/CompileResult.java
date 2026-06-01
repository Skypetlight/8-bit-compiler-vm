package eightBit.vm;

import java.util.List;
import java.util.Map;

public class CompileResult {

    public final List<Object> code;
    public final Map<Integer, Integer> mapping;
    public final Map<String, Integer> labels;

    public CompileResult(List<Object> code, Map<Integer, Integer> mapping, Map<String, Integer> labels) {
        this.code = code;
        this.mapping = mapping;
        this.labels = labels;
    }

    public List<Object> getCode() {
        return code;
    }

    public Map<Integer, Integer> getMapping() {
        return mapping;
    }

    public Map<String, Integer> getLabels() {
        return labels;
    }

}
