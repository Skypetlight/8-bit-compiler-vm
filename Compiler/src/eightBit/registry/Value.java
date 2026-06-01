package eightBit.registry;

public class Value {

	private Object value;
	private int owner;

	public Value(Object v, int o) {
		this.value = v;
		this.owner = o;
	}

	public void setValue(Object v) {
		this.value = v;
	}

	public Object getValue() {
		return this.value;
	}

	public void setOwner(int o) {
		this.owner = o;
	}

	public int getOwner() {
		return this.owner;
	}

	public boolean isType(Object v) {
		return ((this.value instanceof String && this.value instanceof String) || (this.value instanceof Integer && this.value instanceof Integer));
	}

	public boolean compareValue(Object v) {
		if (this.value instanceof String && this.value instanceof String) {
			return (this.value.equals(v));
		}
		if (this.value instanceof Integer && this.value instanceof Integer) {
			return (this.value == v);
		}
		return false;
	}

}
