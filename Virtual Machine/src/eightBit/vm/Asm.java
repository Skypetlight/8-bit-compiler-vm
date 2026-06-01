package eightBit.vm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Asm {

    List<Object> code; // Holds final bytecode
    Map<Integer, Integer> mapping; // Maps instruction index to source line
    Map<String, Integer> labels; // Stores labels and their positions
    Set<String> normalizedLabels; // To detect duplicates

    public Asm() {
        this.code = new ArrayList<>();
        this.mapping = new HashMap<>();
        this.labels = new HashMap<>();;
        this.normalizedLabels = new HashSet<>();
    }

    public Asm(List<Object> code, Map<Integer, Integer> mapping, Map<String, Integer> labels, Set<String> normalizedLabels) {
        this.code = code;
        this.mapping = mapping;
        this.labels = labels;
        this.normalizedLabels = normalizedLabels;
    }

    public int parseNumber(String input) {
        Pattern regexNum = Pattern.compile("^[-+]?[0-9]+$");

        if (input.startsWith("0x")) {
            return Integer.parseInt(input.substring(2), 16);
        } else if (input.startsWith("0o")) {
            return Integer.parseInt(input.substring(2), 8);
        } else if (input.endsWith("b")) {
            return Integer.parseInt(input.substring(0, input.length() - 1), 2);
        } else if (input.endsWith("d")) {
            return Integer.parseInt(input.substring(0, input.length() - 1), 10);
        } else if (regexNum.matcher(input).matches()) {
            return Integer.parseInt(input, 10);
        } else {
            throw new RuntimeException("Invalid number format");
        }
    }

    public String parseLabel(String input) {
        return Pattern.compile("^[.A-Za-z]\\w*$").matcher(input).matches() ? input : null;
    }

    public Integer parseRegister(String input) {
        input = input.toUpperCase();

        switch (input) {
            case "A":
                return 0;
            case "B":
                return 1;
            case "C":
                return 2;
            case "D":
                return 3;
            case "SP":
                return 4;
            default:
                return null;
        }
    }

    public Integer parseOffsetAddressing(String input) {
        input = input.toUpperCase();
        int m = 0;
        int base = 0;

        if (input.charAt(0) == 'A') {
            base = 0;
        } else if (input.charAt(0) == 'B') {
            base = 1;
        } else if (input.charAt(0) == 'C') {
            base = 2;
        } else if (input.charAt(0) == 'D') {
            base = 3;
        } else if (input.substring(0, 2).equals("SP")) {
            base = 4;
        } else {
            return null;
        }

        int offset_start = 1;
        if (base == 4) {
            offset_start = 2;
        }

        if (input.charAt(offset_start) == '-') {
            m = -1;
        } else if (input.charAt(offset_start) == '+') {
            m = 1;
        } else {
            return null;
        }

        int offset = m * Integer.parseInt(input.substring(offset_start + 1), 10);

        if (offset < -16 || offset > 15)
            throw new RuntimeException("offset must be a value between -16...+15");

        if (offset < 0) {
            offset = 32 + offset; // two's complement representation in 5-bit
        }

        return offset * 8 + base; // shift offset 3 bits right and add code for register
    }

    public RegOrNumber parseRegOrNumber(String input, String typeReg, String typeNumber) {
        Integer register = parseRegister(input);
        if (register != null) {
            return new RegOrNumber(typeReg, register);
        } else {
            String label = parseLabel(input);
            if (label != null) {
                return new RegOrNumber(typeNumber, label);
            } else {
                if (typeReg.equals("regaddress")) {
                    register = parseOffsetAddressing(input);
                    if (register != null) {
                        return new RegOrNumber(typeReg, register);
                    }
                }

                try {
                    int value = parseNumber(input);

                    if (value < 0 || value > 255) {
                        throw new IllegalArgumentException(typeNumber + " must have a value between 0–255");
                    }

                    return new RegOrNumber(typeNumber, value);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Not a " + typeNumber + ": " + input);
                }
            }
        }
    }

    public RegOrNumber getValue(String input) {
        switch (input.substring(0, 1)) {
            case "[": // [number] or [register]
                String address = input.substring(1, input.length() - 1);
                return parseRegOrNumber(address, "regaddress", "address");
            case "\"": // "String"
                String text = input.substring(1, input.length() - 1);
                List<Integer> chars = new ArrayList<>();

                for (int i = 0, l = text.length(); i < l; i++) {
                    chars.add((int) text.charAt(i));
                }

                return new RegOrNumber("numbers", chars);
            case "'": // 'C'
                var character = input.substring(1, input.length() - 1);
                if (character.length() > 1)
                    throw new RuntimeException("Only one character is allowed. Use String instead");

                return new RegOrNumber("number", (int) character.charAt(0));
            default: // REGISTER, NUMBER or LABEL
                return parseRegOrNumber(input, "register", "number");
        }
    }

    public void addLabel(String label) {
        String upperLabel = label.toUpperCase();
        if (normalizedLabels.contains(upperLabel))
            throw new RuntimeException("Duplicate label: " + label);

        if (upperLabel.equals("A") || upperLabel.equals("B") || upperLabel.equals("C") || upperLabel.equals("D"))
            throw new RuntimeException("Label contains keyword: " + upperLabel);

        labels.put(label, code.size());
    }

    public void checkNoExtraArg(String instr, String arg) {
        if (arg != null) {
            throw new RuntimeException(instr + ": too many arguments");
        }
    }

    @SuppressWarnings("unchecked")
    public CompileResult go(String input) {
        Pattern regex = Pattern.compile("^[\\t ]*(?:([.A-Za-z]\\w*)[:])?(?:[\\t ]*([A-Za-z]{2,4})(?:[\\t ]+(\\[([.A-Za-z]\\w*((\\+|-)\\d+)?)\\]|\\\".+?\\\"|\\'.+?\\'|[.A-Za-z0-9]\\w*)(?:[\\t ]*[,][\\t ]*(\\[([.A-Za-z]\\w*((\\+|-)\\d+)?)\\]|\\\".+?\\\"|\\'.+?\\'|[.A-Za-z0-9]\\w*))?)?)?");

        // Split text into code lines
        String[] lines = input.split("\\r?\\n");

        for (int i = 0, l = lines.length; i < l; i++) {
            try {
                Matcher matcher = regex.matcher(lines[i]);
                if (matcher.matches()) {
                    String label = matcher.group(1); // same as label
                    String instruction = matcher.group(2); // same as instruction
                    String op1 = matcher.group(3); // same as match[3]
                    String op2 = matcher.group(7); // same as match[7]

                    if (label != null || instruction != null) {
                        if (label != null) {
                            addLabel(label);
                        }

                        if (instruction != null) {
                            String instr = instruction.toUpperCase();
                            RegOrNumber p1, p2;
                            Opcode opCode;

                            // Add mapping instr pos to line number
                            // Don't do it for DB as this is not a real instruction
                            if (!"DB".equals(instr)) {
                                mapping.put(code.size(), i);
                            }

                            switch (instr) {
                                case "DB":
                                    p1 = getValue(op1);

                                    if (p1.type.equals("number"))
                                        code.add(p1.value);
                                    else if (p1.type.equals("numbers")) {
                                        List<Integer> values = (List<Integer>) p1.value;
                                        for (int j = 0, k = values.size(); j < k; j++) {
                                            code.add(values.get(j));
                                        }
                                    } else
                                        throw new RuntimeException("DB does not support this operand");

                                    break;
                                case "HLT":
                                    checkNoExtraArg("HLT", op1);
                                    opCode = Opcode.NONE;
                                    code.add(opCode.code);
                                    break;

                                case "MOV":
                                    p1 = getValue(op1);
                                    p2 = getValue(op2);

                                    if (p1.type.equals("register") && p2.type.equals("register"))
                                        opCode = Opcode.MOV_REG_TO_REG;
                                    else if (p1.type.equals("register") && p2.type.equals("address"))
                                        opCode = Opcode.MOV_ADDRESS_TO_REG;
                                    else if (p1.type.equals("register") && p2.type.equals("regaddress"))
                                        opCode = Opcode.MOV_REGADDRESS_TO_REG;
                                    else if (p1.type.equals("address") && p2.type.equals("register"))
                                        opCode = Opcode.MOV_REG_TO_ADDRESS;
                                    else if (p1.type.equals("regaddress") && p2.type.equals("register"))
                                        opCode = Opcode.MOV_REG_TO_REGADDRESS;
                                    else if (p1.type.equals("register") && p2.type.equals("number"))
                                        opCode = Opcode.MOV_NUMBER_TO_REG;
                                    else if (p1.type.equals("address") && p2.type.equals("number"))
                                        opCode = Opcode.MOV_NUMBER_TO_ADDRESS;
                                    else if (p1.type.equals("regaddress") && p2.type.equals("number"))
                                        opCode = Opcode.MOV_NUMBER_TO_REGADDRESS;
                                    else
                                        throw new RuntimeException("MOV does not support this operands");

                                    code.add(opCode.code);
                                    code.add(p1.value);
                                    code.add(p2.value);
                                    break;
                                case "ADD":
                                    p1 = getValue(op1);
                                    p2 = getValue(op2);

                                    if (p1.type.equals("register") && p2.type.equals("register"))
                                        opCode = Opcode.ADD_REG_TO_REG;
                                    else if (p1.type.equals("register") && p2.type.equals("regaddress"))
                                        opCode = Opcode.ADD_REGADDRESS_TO_REG;
                                    else if (p1.type.equals("register") && p2.type.equals("address"))
                                        opCode = Opcode.ADD_ADDRESS_TO_REG;
                                    else if (p1.type.equals("register") && p2.type.equals("number"))
                                        opCode = Opcode.ADD_NUMBER_TO_REG;
                                    else
                                        throw new RuntimeException("ADD does not support this operands");

                                    code.add(opCode.code);
                                    code.add(p1.value);
                                    code.add(p2.value);
                                    break;
                                case "SUB":
                                    p1 = getValue(op1);
                                    p2 = getValue(op2);

                                    if (p1.type.equals("register") && p2.type.equals("register"))
                                        opCode = Opcode.SUB_REG_FROM_REG;
                                    else if (p1.type.equals("register") && p2.type.equals("regaddress"))
                                        opCode = Opcode.SUB_REGADDRESS_FROM_REG;
                                    else if (p1.type.equals("register") && p2.type.equals("address"))
                                        opCode = Opcode.SUB_ADDRESS_FROM_REG;
                                    else if (p1.type.equals("register") && p2.type.equals("number"))
                                        opCode = Opcode.SUB_NUMBER_FROM_REG;
                                    else
                                        throw new RuntimeException("SUB does not support this operands");

                                    code.add(opCode.code);
                                    code.add(p1.value);
                                    code.add(p2.value);
                                    break;
                                case "INC":
                                    p1 = getValue(op1);
                                    checkNoExtraArg("INC", op2);

                                    if (p1.type.equals("register"))
                                        opCode = Opcode.INC_REG;
                                    else
                                        throw new RuntimeException("INC does not support this operand");

                                    code.add(opCode.code);
                                    code.add(p1.value);

                                    break;
                                case "DEC":
                                    p1 = getValue(op1);
                                    checkNoExtraArg("DEC", op2);

                                    if (p1.type.equals("register"))
                                        opCode = Opcode.DEC_REG;
                                    else
                                        throw new RuntimeException("DEC does not support this operand");

                                    code.add(opCode.code);
                                    code.add(p1.value);

                                    break;
                                case "CMP":
                                    p1 = getValue(op1);
                                    p2 = getValue(op2);

                                    if (p1.type.equals("register") && p2.type.equals("register"))
                                        opCode = Opcode.CMP_REG_WITH_REG;
                                    else if (p1.type.equals("register") && p2.type.equals("regaddress"))
                                        opCode = Opcode.CMP_REGADDRESS_WITH_REG;
                                    else if (p1.type.equals("register") && p2.type.equals("address"))
                                        opCode = Opcode.CMP_ADDRESS_WITH_REG;
                                    else if (p1.type.equals("register") && p2.type.equals("number"))
                                        opCode = Opcode.CMP_NUMBER_WITH_REG;
                                    else
                                        throw new RuntimeException("CMP does not support this operands");

                                    code.add(opCode.code);
                                    code.add(p1.value);
                                    code.add(p2.value);
                                    break;
                                case "JMP":
                                    p1 = getValue(op1);
                                    checkNoExtraArg("JMP", op2);

                                    if (p1.type.equals("register"))
                                        opCode = Opcode.JMP_REGADDRESS;
                                    else if (p1.type.equals("number"))
                                        opCode = Opcode.JMP_ADDRESS;
                                    else
                                        throw new RuntimeException("JMP does not support this operands");

                                    code.add(opCode.code);
                                    code.add(p1.value);
                                    break;
                                case "JC":
                                case "JB":
                                case "JNAE":
                                    p1 = getValue(op1);
                                    checkNoExtraArg(instr, op2);

                                    if (p1.type.equals("register"))
                                        opCode = Opcode.JC_REGADDRESS;
                                    else if (p1.type.equals("number"))
                                        opCode = Opcode.JC_ADDRESS;
                                    else
                                        throw new RuntimeException(instr + " does not support this operand");

                                    code.add(opCode.code);
                                    code.add(p1.value);
                                    break;
                                case "JNC":
                                case "JNB":
                                case "JAE":
                                    p1 = getValue(op1);
                                    checkNoExtraArg(instr, op2);

                                    if (p1.type.equals("register"))
                                        opCode = Opcode.JNC_REGADDRESS;
                                    else if (p1.type.equals("number"))
                                        opCode = Opcode.JNC_ADDRESS;
                                    else
                                        throw new RuntimeException(instr + "does not support this operand");

                                    code.add(opCode.code);
                                    code.add(p1.value);
                                    break;
                                case "JZ":
                                case "JE":
                                    p1 = getValue(op1);
                                    checkNoExtraArg(instr, op2);

                                    if (p1.type.equals("register"))
                                        opCode = Opcode.JZ_REGADDRESS;
                                    else if (p1.type.equals("number"))
                                        opCode = Opcode.JZ_ADDRESS;
                                    else
                                        throw new RuntimeException(instr + " does not support this operand");

                                    code.add(opCode.code);
                                    code.add(p1.value);
                                    break;
                                case "JNZ":
                                case "JNE":
                                    p1 = getValue(op1);
                                    checkNoExtraArg(instr, op2);

                                    if (p1.type.equals("register"))
                                        opCode = Opcode.JNZ_REGADDRESS;
                                    else if (p1.type.equals("number"))
                                        opCode = Opcode.JNZ_ADDRESS;
                                    else
                                        throw new RuntimeException(instr + " does not support this operand");

                                    code.add(opCode.code);
                                    code.add(p1.value);
                                    break;
                                case "JA":
                                case "JNBE":
                                    p1 = getValue(op1);
                                    checkNoExtraArg(instr, op2);

                                    if (p1.type.equals("register"))
                                        opCode = Opcode.JA_REGADDRESS;
                                    else if (p1.type.equals("number"))
                                        opCode = Opcode.JA_ADDRESS;
                                    else
                                        throw new RuntimeException(instr + " does not support this operand");

                                    code.add(opCode.code);
                                    code.add(p1.value);
                                    break;
                                case "JNA":
                                case "JBE":
                                    p1 = getValue(op1);
                                    checkNoExtraArg(instr, op2);

                                    if (p1.type.equals("register"))
                                        opCode = Opcode.JNA_REGADDRESS;
                                    else if (p1.type.equals("number"))
                                        opCode = Opcode.JNA_ADDRESS;
                                    else
                                        throw new RuntimeException(instr + " does not support this operand");

                                    code.add(opCode.code);
                                    code.add(p1.value);
                                    break;
                                case "PUSH":
                                    p1 = getValue(op1);
                                    checkNoExtraArg(instr, op2);

                                    if (p1.type.equals("register"))
                                        opCode = Opcode.PUSH_REG;
                                    else if (p1.type.equals("regaddress"))
                                        opCode = Opcode.PUSH_REGADDRESS;
                                    else if (p1.type.equals("address"))
                                        opCode = Opcode.PUSH_ADDRESS;
                                    else if (p1.type.equals("number"))
                                        opCode = Opcode.PUSH_NUMBER;
                                    else
                                        throw new RuntimeException("PUSH does not support this operand");

                                    code.add(opCode.code);
                                    code.add(p1.value);
                                    break;
                                case "POP":
                                    p1 = getValue(op1);
                                    checkNoExtraArg(instr, op2);

                                    if (p1.type.equals("register"))
                                        opCode = Opcode.POP_REG;
                                    else
                                        throw new RuntimeException("PUSH does not support this operand");

                                    code.add(opCode.code);
                                    code.add(p1.value);
                                    break;
                                case "CALL":
                                    p1 = getValue(op1);
                                    checkNoExtraArg(instr, op2);

                                    if (p1.type.equals("register"))
                                        opCode = Opcode.CALL_REGADDRESS;
                                    else if (p1.type.equals("number"))
                                        opCode = Opcode.CALL_ADDRESS;
                                    else
                                        throw new RuntimeException("CALL does not support this operand");

                                    code.add(opCode.code);
                                    code.add(p1.value);
                                    break;
                                case "RET":
                                    checkNoExtraArg(instr, op1);

                                    opCode = Opcode.RET;

                                    code.add(opCode.code);
                                    break;

                                case "MUL":
                                    p1 = getValue(op1);
                                    checkNoExtraArg(instr, op2);

                                    if (p1.type.equals("register"))
                                        opCode = Opcode.MUL_REG;
                                    else if (p1.type.equals("regaddress"))
                                        opCode = Opcode.MUL_REGADDRESS;
                                    else if (p1.type.equals("address"))
                                        opCode = Opcode.MUL_ADDRESS;
                                    else if (p1.type.equals("number"))
                                        opCode = Opcode.MUL_NUMBER;
                                    else
                                        throw new RuntimeException("MULL does not support this operand");

                                    code.add(opCode.code);
                                    code.add(p1.value);
                                    break;
                                case "DIV":
                                    p1 = getValue(op1);
                                    checkNoExtraArg(instr, op2);

                                    if (p1.type.equals("register"))
                                        opCode = Opcode.DIV_REG;
                                    else if (p1.type.equals("regaddress"))
                                        opCode = Opcode.DIV_REGADDRESS;
                                    else if (p1.type.equals("address"))
                                        opCode = Opcode.DIV_ADDRESS;
                                    else if (p1.type.equals("number"))
                                        opCode = Opcode.DIV_NUMBER;
                                    else
                                        throw new RuntimeException("DIV does not support this operand");

                                    code.add(opCode.code);
                                    code.add(p1.value);
                                    break;
                                case "AND":
                                    p1 = getValue(op1);
                                    p2 = getValue(op2);

                                    if (p1.type.equals("register") && p2.type.equals("register"))
                                        opCode = Opcode.AND_REG_WITH_REG;
                                    else if (p1.type.equals("register") && p2.type.equals("regaddress"))
                                        opCode = Opcode.AND_REGADDRESS_WITH_REG;
                                    else if (p1.type.equals("register") && p2.type.equals("address"))
                                        opCode = Opcode.AND_ADDRESS_WITH_REG;
                                    else if (p1.type.equals("register") && p2.type.equals("number"))
                                        opCode = Opcode.AND_NUMBER_WITH_REG;
                                    else
                                        throw new RuntimeException("AND does not support this operands");

                                    code.add(opCode.code);
                                    code.add(p1.value);
                                    code.add(p2.value);
                                    break;
                                case "OR":
                                    p1 = getValue(op1);
                                    p2 = getValue(op2);

                                    if (p1.type.equals("register") && p2.type.equals("register"))
                                        opCode = Opcode.OR_REG_WITH_REG;
                                    else if (p1.type.equals("register") && p2.type.equals("regaddress"))
                                        opCode = Opcode.OR_REGADDRESS_WITH_REG;
                                    else if (p1.type.equals("register") && p2.type.equals("address"))
                                        opCode = Opcode.OR_ADDRESS_WITH_REG;
                                    else if (p1.type.equals("register") && p2.type.equals("number"))
                                        opCode = Opcode.OR_NUMBER_WITH_REG;
                                    else
                                        throw new RuntimeException("OR does not support this operands");

                                    code.add(opCode.code);
                                    code.add(p1.value);
                                    code.add(p2.value);
                                    break;
                                case "XOR":
                                    p1 = getValue(op1);
                                    p2 = getValue(op2);

                                    if (p1.type.equals("register") && p2.type.equals("register"))
                                        opCode = Opcode.XOR_REG_WITH_REG;
                                    else if (p1.type.equals("register") && p2.type.equals("regaddress"))
                                        opCode = Opcode.XOR_REGADDRESS_WITH_REG;
                                    else if (p1.type.equals("register") && p2.type.equals("address"))
                                        opCode = Opcode.XOR_ADDRESS_WITH_REG;
                                    else if (p1.type.equals("register") && p2.type.equals("number"))
                                        opCode = Opcode.XOR_NUMBER_WITH_REG;
                                    else
                                        throw new RuntimeException("XOR does not support this operands");

                                    code.add(opCode.code);
                                    code.add(p1.value);
                                    code.add(p2.value);
                                    break;
                                case "NOT":
                                    p1 = getValue(op1);
                                    checkNoExtraArg(instr, op2);

                                    if (p1.type.equals("register"))
                                        opCode = Opcode.NOT_REG;
                                    else
                                        throw new RuntimeException("NOT does not support this operand");

                                    code.add(opCode.code);
                                    code.add(p1.value);
                                    break;
                                case "SHL":
                                case "SAL":
                                    p1 = getValue(op1);
                                    p2 = getValue(op2);

                                    if (p1.type.equals("register") && p2.type.equals("register"))
                                        opCode = Opcode.SHL_REG_WITH_REG;
                                    else if (p1.type.equals("register") && p2.type.equals("regaddress"))
                                        opCode = Opcode.SHL_REGADDRESS_WITH_REG;
                                    else if (p1.type.equals("register") && p2.type.equals("address"))
                                        opCode = Opcode.SHL_ADDRESS_WITH_REG;
                                    else if (p1.type.equals("register") && p2.type.equals("number"))
                                        opCode = Opcode.SHL_NUMBER_WITH_REG;
                                    else
                                        throw new RuntimeException(instr + " does not support this operands");

                                    code.add(opCode.code);
                                    code.add(p1.value);
                                    code.add(p2.value);
                                    break;
                                case "SHR":
                                case "SAR":
                                    p1 = getValue(op1);
                                    p2 = getValue(op2);

                                    if (p1.type.equals("register") && p2.type.equals("register"))
                                        opCode = Opcode.SHR_REG_WITH_REG;
                                    else if (p1.type.equals("register") && p2.type.equals("regaddress"))
                                        opCode = Opcode.SHR_REGADDRESS_WITH_REG;
                                    else if (p1.type.equals("register") && p2.type.equals("address"))
                                        opCode = Opcode.SHR_ADDRESS_WITH_REG;
                                    else if (p1.type.equals("register") && p2.type.equals("number"))
                                        opCode = Opcode.SHR_NUMBER_WITH_REG;
                                    else
                                        throw new RuntimeException(instr + " does not support this operands");

                                    code.add(opCode.code);
                                    code.add(p1.value);
                                    code.add(p2.value);
                                    break;
                                default:
                                    throw new RuntimeException("Invalid instruction: " + instruction);
                            }
                        }
                    } else {
                        // Check if line starts with a comment otherwise the line contains an error and
                        // can not be parsed
                        String line = lines[i].trim();
                        if (line != "" && line.substring(0, 1) != ";") {
                            throw new RuntimeException("Syntax error");
                        }
                    }
                }
            } catch (ParseException e) {
                System.err.println("Assembly error on line " + e.getLine() + ": " + e.getReason());
            } catch (Exception e) {
                System.err.println("Execution error: " + e.getMessage());
            }
        }

        for (int i = 0; i < code.size(); i++) {
            Object value = code.get(i);
            if (!(value instanceof Integer)) {
                String label = (String) value;
                Integer address = labels.get(label);
                if (address == null) {
                    throw new RuntimeException("Undefined label: " + label);
                }
                code.set(i, address);
            }
        }

        return new CompileResult(code, mapping, labels);
    }
}
