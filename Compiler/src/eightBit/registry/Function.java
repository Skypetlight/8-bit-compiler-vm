package eightBit.registry;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Function {

	private int countConstants;
	private HashMap<String, Value> constants;
	private HashMap<String, Value> variables;

	public Function() {
		this.countConstants = 0;
		this.constants = new HashMap<>();
		this.variables = new HashMap<>();
	}

	// Add methods
	public String addConstant(Object v, int o) {
		String name = ((this.countConstants++) + "C");
		this.constants.put(name, new Value(v, o));
		return name;
	}

	public void addVariable(String n, Object v, int o) {
		this.variables.put(n, new Value(v, o));
	}
	// End of add methods

	// Find methods
	public Value findConstant(String n) {
		return this.constants.get(n);
	}

	public Value findVariable(String n) {
		return this.variables.get(n);
	}

	public String findConstantByValue(Object v) {
		return constants.keySet().stream().filter(e -> constants.get(e).compareValue(v)).findFirst().orElse(null);
	}

	public String findVariableByValue(Object v) {
		return this.variables.keySet().stream().filter(e -> variables.get(e).compareValue(v)).findFirst().orElse(null);
	}
	// End of find methods

	// Get methods
	public List<String> getConstantsToUse(int n) {
		return this.constants.keySet().stream()
				.filter(e -> constants.get(e).getOwner() == n)
				.sorted()
				.collect(Collectors.toList());
	}

	public List<String> getVariablesToUse(int n) {
		return this.variables.keySet().stream()
				.filter(e -> variables.get(e).getOwner() == n)
				.sorted((a, b) -> {
					if (a.equals("0"))
						return -1;
					if (b.equals("0"))
						return 1;
					return a.compareTo(b);
				})
				.collect(Collectors.toList());
	}
	// End of get methods
}
