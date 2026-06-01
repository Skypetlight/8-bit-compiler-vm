package eightBit.vm;

public class RegOrNumber {

    public final String type;
    public final Object value;

    public RegOrNumber(String type, Object value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return "{type: " + type + ", value: " + value + "}";
    }

}
