package eightBit.vm;

import java.util.Arrays;
import java.util.List;

public class Memory {

    private int[] data;

    public Memory() {
        this.data = new int[256];
    }

    public Memory(int[] data) {
        this.data = data;
    }

    public int[] getData() {
        return data;
    }

    public void setData(int[] data) {
        this.data = data;
    }

    public int load(int address) {
        if (address < 0 || address >= data.length)
            throw new RuntimeException("Memory access violation");
        return data[address];
    }

    public void store(int address, int value) {
        if (address < 0 || address >= data.length)
            throw new RuntimeException("Memory access violation");
        data[address] = value;
    }

    public void reset() {
        Arrays.fill(data, 0);
    }

    public void loadProgram(List<Object> code) {
        if (code.size() > data.length)
            throw new RuntimeException("Program too large for memory");

        for (int i = 0; i < code.size(); i++) {
            Object obj = code.get(i);
            if (!(obj instanceof Integer)) {
                throw new RuntimeException("Non-integer value found in code at index " + i + ": " + obj);
            }
            data[i] = (Integer) obj; // Safe cast
        }
    }

}
