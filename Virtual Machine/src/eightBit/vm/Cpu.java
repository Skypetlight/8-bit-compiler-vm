package eightBit.vm;

public class Cpu {

    private final int maxSP = 231; // Max stack pointer (upper stack bound)
    private final int minSP = 0; // Min stack pointer (lower stack bound)

    private int[] gpr; // General-purpose registers (R0–R3)
    private int sp; // Stack pointer
    private int ip; // Instruction pointer
    private boolean zero; // Zero flag (Z)
    private boolean carry; // Carry flag (C)
    private boolean fault; // Fault state flag
    private boolean halted; // Halted state flag
    private Memory memory;

    public Cpu() {
        this.gpr = new int[4];
        for (int i = 0; i < this.gpr.length; i++)
            this.gpr[i] = 0;

        this.sp = this.maxSP;
        this.ip = 0;
        this.zero = false;
        this.carry = false;
        this.fault = false;
        this.halted = false;
        this.memory = new Memory();
    }

    public Cpu(Memory memory) {
        this.gpr = new int[4];
        for (int i = 0; i < this.gpr.length; i++)
            this.gpr[i] = 0;

        this.sp = this.maxSP;
        this.ip = 0;
        this.zero = false;
        this.carry = false;
        this.fault = false;
        this.halted = false;
        this.memory = memory;
    }

    public Cpu(int[] gpr, int sp, int ip, boolean zero, boolean carry, boolean fault, Memory memory) {
        this.gpr = gpr;
        this.sp = sp;
        this.ip = ip;
        this.zero = zero;
        this.carry = carry;
        this.fault = fault;
        this.halted = false;
        this.memory = memory;
    }

    public void reset() {
        for (int i = 0; i < this.gpr.length; i++)
            this.gpr[i] = 0;

        this.sp = this.maxSP;
        this.ip = 0;
        this.zero = false;
        this.carry = false;
        this.fault = false;
        this.halted = false;
    }

    public int getSp() {
        return sp;
    }

    public boolean isHalted() {
        return halted;
    }

    public int checkGPR(int reg) {
        if (reg < 0 || reg >= gpr.length) {
            throw new RuntimeException("Invalid register: " + reg);
        } else {
            return reg;
        }
    }

    public int checkGPR_SP(int reg) {
        if (reg < 0 || reg >= 1 + gpr.length) {
            throw new RuntimeException("Invalid register: " + reg);
        } else {
            return reg;
        }
    }

    public void setGPR_SP(int reg, int value) {
        if (reg >= 0 && reg < gpr.length) {
            gpr[reg] = value;
        } else if (reg == gpr.length) {
            sp = value;

            // Not likely to happen, since we always get here after checkOpertion().
            if (sp < minSP) {
                throw new RuntimeException("Stack overflow");
            } else if (sp > maxSP) {
                throw new RuntimeException("Stack underflow");
            }
        } else {
            throw new RuntimeException("Invalid register: " + reg);
        }
    }

    public int getGPR_SP(int reg) {
        if (reg >= 0 && reg < gpr.length) {
            return gpr[reg];
        } else if (reg == gpr.length) {
            return sp;
        } else {
            throw new RuntimeException("Invalid register: " + reg);
        }
    }

    public int indirectRegisterAddress(int value) {
        int reg = value % 8;

        int base;
        if (reg < gpr.length) {
            base = gpr[reg];
        } else {
            base = sp;
        }

        int offset = value / 8;
        if (offset > 15) {
            offset = offset - 32;
        }

        return base + offset;
    }

    public int checkOperation(int value) {
        zero = false;
        carry = false;

        if (value >= 256) {
            carry = true;
            value = value % 256;
        } else if (value == 0) {
            zero = true;
        } else if (value < 0) {
            carry = true;
            value = 256 - (-value) % 256;
        }

        return value;
    }

    public void jump(int newIP) {
        if (newIP < 0 || newIP >= memory.getData().length) {
            throw new RuntimeException("IP outside memory");
        } else {
            ip = newIP;
        }
    }

    public void push(int value) {
        memory.store(sp--, value);
        if (sp < minSP) {
            throw new RuntimeException("Stack overflow");
        }
    }

    public int pop() {
        Object value = memory.load(++sp);
        if (sp > maxSP) {
            throw new RuntimeException("Stack underflow");
        }
        return (int) value;
    }

    public int division(int divisor) {
        if (divisor == 0) {
            throw new RuntimeException("Division by 0");
        }

        return gpr[0] / divisor; // integer division
    }

    public void step() {
        if (halted)
            return;

        if (fault == true) {
            throw new RuntimeException("FAULT. Reset to continue.");
        }

        try {
            if (ip < 0 || ip >= memory.getData().length) {
                throw new RuntimeException("Instruction pointer is outside of memory");
            }

            int regTo, regFrom, memFrom, memTo, number;
            Opcode instr = Opcode.fromCode(memory.load(ip));
            switch (instr) {
                case Opcode.NONE:
                    halted = true;
                    return; // Abort step
                case Opcode.MOV_REG_TO_REG:
                    regTo = checkGPR_SP(memory.load(++ip));
                    regFrom = checkGPR_SP(memory.load(++ip));
                    setGPR_SP(regTo, getGPR_SP(regFrom));
                    ip++;
                    break;
                case Opcode.MOV_ADDRESS_TO_REG:
                    regTo = checkGPR_SP(memory.load(++ip));
                    memFrom = memory.load(++ip);
                    setGPR_SP(regTo, memory.load(memFrom));
                    ip++;
                    break;
                case Opcode.MOV_REGADDRESS_TO_REG:
                    regTo = checkGPR_SP(memory.load(++ip));
                    regFrom = memory.load(++ip);
                    setGPR_SP(regTo, memory.load(indirectRegisterAddress(regFrom)));
                    ip++;
                    break;
                case Opcode.MOV_REG_TO_ADDRESS:
                    memTo = memory.load(++ip);
                    regFrom = checkGPR_SP(memory.load(++ip));
                    memory.store(memTo, getGPR_SP(regFrom));
                    ip++;
                    break;
                case Opcode.MOV_REG_TO_REGADDRESS:
                    regTo = memory.load(++ip);
                    regFrom = checkGPR_SP(memory.load(++ip));
                    memory.store(indirectRegisterAddress(regTo), getGPR_SP(regFrom));
                    ip++;
                    break;
                case Opcode.MOV_NUMBER_TO_REG:
                    regTo = checkGPR_SP(memory.load(++ip));
                    number = memory.load(++ip);
                    setGPR_SP(regTo, number);
                    ip++;
                    break;
                case Opcode.MOV_NUMBER_TO_ADDRESS:
                    memTo = memory.load(++ip);
                    number = memory.load(++ip);
                    memory.store(memTo, number);
                    ip++;
                    break;
                case Opcode.MOV_NUMBER_TO_REGADDRESS:
                    regTo = memory.load(++ip);
                    number = memory.load(++ip);
                    memory.store(indirectRegisterAddress(regTo), number);
                    ip++;
                    break;
                case Opcode.ADD_REG_TO_REG:
                    regTo = checkGPR_SP(memory.load(++ip));
                    regFrom = checkGPR_SP(memory.load(++ip));
                    setGPR_SP(regTo, checkOperation(getGPR_SP(regTo) + getGPR_SP(regFrom)));
                    ip++;
                    break;
                case Opcode.ADD_REGADDRESS_TO_REG:
                    regTo = checkGPR_SP(memory.load(++ip));
                    regFrom = memory.load(++ip);
                    setGPR_SP(regTo, checkOperation(getGPR_SP(regTo) + memory.load(indirectRegisterAddress(regFrom))));
                    ip++;
                    break;
                case Opcode.ADD_ADDRESS_TO_REG:
                    regTo = checkGPR_SP(memory.load(++ip));
                    memFrom = memory.load(++ip);
                    setGPR_SP(regTo, checkOperation(getGPR_SP(regTo) + memory.load(memFrom)));
                    ip++;
                    break;
                case Opcode.ADD_NUMBER_TO_REG:
                    regTo = checkGPR_SP(memory.load(++ip));
                    number = memory.load(++ip);
                    setGPR_SP(regTo, checkOperation(getGPR_SP(regTo) + number));
                    ip++;
                    break;
                case Opcode.SUB_REG_FROM_REG:
                    regTo = checkGPR_SP(memory.load(++ip));
                    regFrom = checkGPR_SP(memory.load(++ip));
                    setGPR_SP(regTo, checkOperation(getGPR_SP(regTo) - gpr[regFrom]));
                    ip++;
                    break;
                case Opcode.SUB_REGADDRESS_FROM_REG:
                    regTo = checkGPR_SP(memory.load(++ip));
                    regFrom = memory.load(++ip);
                    setGPR_SP(regTo, checkOperation(getGPR_SP(regTo) - memory.load(indirectRegisterAddress(regFrom))));
                    ip++;
                    break;
                case Opcode.SUB_ADDRESS_FROM_REG:
                    regTo = checkGPR_SP(memory.load(++ip));
                    memFrom = memory.load(++ip);
                    setGPR_SP(regTo, checkOperation(getGPR_SP(regTo) - memory.load(memFrom)));
                    ip++;
                    break;
                case Opcode.SUB_NUMBER_FROM_REG:
                    regTo = checkGPR_SP(memory.load(++ip));
                    number = memory.load(++ip);
                    setGPR_SP(regTo, checkOperation(getGPR_SP(regTo) - number));
                    ip++;
                    break;
                case Opcode.INC_REG:
                    regTo = checkGPR_SP(memory.load(++ip));
                    setGPR_SP(regTo, checkOperation(getGPR_SP(regTo) + 1));
                    ip++;
                    break;
                case Opcode.DEC_REG:
                    regTo = checkGPR_SP(memory.load(++ip));
                    setGPR_SP(regTo, checkOperation(getGPR_SP(regTo) - 1));
                    ip++;
                    break;
                case Opcode.CMP_REG_WITH_REG:
                    regTo = checkGPR_SP(memory.load(++ip));
                    regFrom = checkGPR_SP(memory.load(++ip));
                    checkOperation(getGPR_SP(regTo) - getGPR_SP(regFrom));
                    ip++;
                    break;
                case Opcode.CMP_REGADDRESS_WITH_REG:
                    regTo = checkGPR_SP(memory.load(++ip));
                    regFrom = memory.load(++ip);
                    checkOperation(getGPR_SP(regTo) - memory.load(indirectRegisterAddress(regFrom)));
                    ip++;
                    break;
                case Opcode.CMP_ADDRESS_WITH_REG:
                    regTo = checkGPR_SP(memory.load(++ip));
                    memFrom = memory.load(++ip);
                    checkOperation(getGPR_SP(regTo) - memory.load(memFrom));
                    ip++;
                    break;
                case Opcode.CMP_NUMBER_WITH_REG:
                    regTo = checkGPR_SP(memory.load(++ip));
                    number = memory.load(++ip);
                    checkOperation(getGPR_SP(regTo) - number);
                    ip++;
                    break;
                case Opcode.JMP_REGADDRESS:
                    regTo = checkGPR(memory.load(++ip));
                    jump(gpr[regTo]);
                    break;
                case Opcode.JMP_ADDRESS:
                    number = memory.load(++ip);
                    jump(number);
                    break;
                case Opcode.JC_REGADDRESS:
                    regTo = checkGPR(memory.load(++ip));
                    if (carry) {
                        jump(gpr[regTo]);
                    } else {
                        ip++;
                    }
                    break;
                case Opcode.JC_ADDRESS:
                    number = memory.load(++ip);
                    if (carry) {
                        jump(number);
                    } else {
                        ip++;
                    }
                    break;
                case Opcode.JNC_REGADDRESS:
                    regTo = checkGPR(memory.load(++ip));
                    if (!carry) {
                        jump(gpr[regTo]);
                    } else {
                        ip++;
                    }
                    break;
                case Opcode.JNC_ADDRESS:
                    number = memory.load(++ip);
                    if (!carry) {
                        jump(number);
                    } else {
                        ip++;
                    }
                    break;
                case Opcode.JZ_REGADDRESS:
                    regTo = checkGPR(memory.load(++ip));
                    if (zero) {
                        jump(gpr[regTo]);
                    } else {
                        ip++;
                    }
                    break;
                case Opcode.JZ_ADDRESS:
                    number = memory.load(++ip);
                    if (zero) {
                        jump(number);
                    } else {
                        ip++;
                    }
                    break;
                case Opcode.JNZ_REGADDRESS:
                    regTo = checkGPR(memory.load(++ip));
                    if (!zero) {
                        jump(gpr[regTo]);
                    } else {
                        ip++;
                    }
                    break;
                case Opcode.JNZ_ADDRESS:
                    number = memory.load(++ip);
                    if (!zero) {
                        jump(number);
                    } else {
                        ip++;
                    }
                    break;
                case Opcode.JA_REGADDRESS:
                    regTo = checkGPR(memory.load(++ip));
                    if (!zero && !carry) {
                        jump(gpr[regTo]);
                    } else {
                        ip++;
                    }
                    break;
                case Opcode.JA_ADDRESS:
                    number = memory.load(++ip);
                    if (!zero && !carry) {
                        jump(number);
                    } else {
                        ip++;
                    }
                    break;
                case Opcode.JNA_REGADDRESS: // JNA REG
                    regTo = checkGPR(memory.load(++ip));
                    if (zero || carry) {
                        jump(gpr[regTo]);
                    } else {
                        ip++;
                    }
                    break;
                case Opcode.JNA_ADDRESS:
                    number = memory.load(++ip);
                    if (zero || carry) {
                        jump(number);
                    } else {
                        ip++;
                    }
                    break;
                case Opcode.PUSH_REG:
                    regFrom = checkGPR(memory.load(++ip));
                    push(gpr[regFrom]);
                    ip++;
                    break;
                case Opcode.PUSH_REGADDRESS:
                    regFrom = memory.load(++ip);
                    push(memory.load(indirectRegisterAddress(regFrom)));
                    ip++;
                    break;
                case Opcode.PUSH_ADDRESS:
                    memFrom = memory.load(++ip);
                    push(memory.load(memFrom));
                    ip++;
                    break;
                case Opcode.PUSH_NUMBER:
                    number = memory.load(++ip);
                    push(number);
                    ip++;
                    break;
                case Opcode.POP_REG:
                    regTo = checkGPR(memory.load(++ip));
                    gpr[regTo] = pop();
                    ip++;
                    break;
                case Opcode.CALL_REGADDRESS:
                    regTo = checkGPR(memory.load(++ip));
                    push(ip + 1);
                    jump(gpr[regTo]);
                    break;
                case Opcode.CALL_ADDRESS:
                    number = memory.load(++ip);
                    push(ip + 1);
                    jump(number);
                    break;
                case Opcode.RET:
                    jump(pop());
                    break;
                case Opcode.MUL_REG: // A = A * REG
                    regFrom = checkGPR(memory.load(++ip));
                    gpr[0] = checkOperation(gpr[0] * gpr[regFrom]);
                    ip++;
                    break;
                case Opcode.MUL_REGADDRESS: // A = A * [REG]
                    regFrom = memory.load(++ip);
                    gpr[0] = checkOperation(gpr[0] * memory.load(indirectRegisterAddress(regFrom)));
                    ip++;
                    break;
                case Opcode.MUL_ADDRESS: // A = A * [NUMBER]
                    memFrom = memory.load(++ip);
                    gpr[0] = checkOperation(gpr[0] * memory.load(memFrom));
                    ip++;
                    break;
                case Opcode.MUL_NUMBER: // A = A * NUMBER
                    number = memory.load(++ip);
                    gpr[0] = checkOperation(gpr[0] * number);
                    ip++;
                    break;
                case Opcode.DIV_REG: // A = A / REG
                    regFrom = checkGPR(memory.load(++ip));
                    gpr[0] = checkOperation(division(gpr[regFrom]));
                    ip++;
                    break;
                case Opcode.DIV_REGADDRESS: // A = A / [REG]
                    regFrom = memory.load(++ip);
                    gpr[0] = checkOperation(division(memory.load(indirectRegisterAddress(regFrom))));
                    ip++;
                    break;
                case Opcode.DIV_ADDRESS: // A = A / [NUMBER]
                    memFrom = memory.load(++ip);
                    gpr[0] = checkOperation(division(memory.load(memFrom)));
                    ip++;
                    break;
                case Opcode.DIV_NUMBER: // A = A / NUMBER
                    number = memory.load(++ip);
                    gpr[0] = checkOperation(division(number));
                    ip++;
                    break;
                case Opcode.AND_REG_WITH_REG:
                    regTo = checkGPR(memory.load(++ip));
                    regFrom = checkGPR(memory.load(++ip));
                    gpr[regTo] = checkOperation(gpr[regTo] & gpr[regFrom]);
                    ip++;
                    break;
                case Opcode.AND_REGADDRESS_WITH_REG:
                    regTo = checkGPR(memory.load(++ip));
                    regFrom = memory.load(++ip);
                    gpr[regTo] = checkOperation(gpr[regTo] & memory.load(indirectRegisterAddress(regFrom)));
                    ip++;
                    break;
                case Opcode.AND_ADDRESS_WITH_REG:
                    regTo = checkGPR(memory.load(++ip));
                    memFrom = memory.load(++ip);
                    gpr[regTo] = checkOperation(gpr[regTo] & memory.load(memFrom));
                    ip++;
                    break;
                case Opcode.AND_NUMBER_WITH_REG:
                    regTo = checkGPR(memory.load(++ip));
                    number = memory.load(++ip);
                    gpr[regTo] = checkOperation(gpr[regTo] & number);
                    ip++;
                    break;
                case Opcode.OR_REG_WITH_REG:
                    regTo = checkGPR(memory.load(++ip));
                    regFrom = checkGPR(memory.load(++ip));
                    gpr[regTo] = checkOperation(gpr[regTo] | gpr[regFrom]);
                    ip++;
                    break;
                case Opcode.OR_REGADDRESS_WITH_REG:
                    regTo = checkGPR(memory.load(++ip));
                    regFrom = memory.load(++ip);
                    gpr[regTo] = checkOperation(gpr[regTo] | memory.load(indirectRegisterAddress(regFrom)));
                    ip++;
                    break;
                case Opcode.OR_ADDRESS_WITH_REG:
                    regTo = checkGPR(memory.load(++ip));
                    memFrom = memory.load(++ip);
                    gpr[regTo] = checkOperation(gpr[regTo] | memory.load(memFrom));
                    ip++;
                    break;
                case Opcode.OR_NUMBER_WITH_REG:
                    regTo = checkGPR(memory.load(++ip));
                    number = memory.load(++ip);
                    gpr[regTo] = checkOperation(gpr[regTo] | number);
                    ip++;
                    break;
                case Opcode.XOR_REG_WITH_REG:
                    regTo = checkGPR(memory.load(++ip));
                    regFrom = checkGPR(memory.load(++ip));
                    gpr[regTo] = checkOperation(gpr[regTo] ^ gpr[regFrom]);
                    ip++;
                    break;
                case Opcode.XOR_REGADDRESS_WITH_REG:
                    regTo = checkGPR(memory.load(++ip));
                    regFrom = memory.load(++ip);
                    gpr[regTo] = checkOperation(gpr[regTo] ^ memory.load(indirectRegisterAddress(regFrom)));
                    ip++;
                    break;
                case Opcode.XOR_ADDRESS_WITH_REG:
                    regTo = checkGPR(memory.load(++ip));
                    memFrom = memory.load(++ip);
                    gpr[regTo] = checkOperation(gpr[regTo] ^ memory.load(memFrom));
                    ip++;
                    break;
                case Opcode.XOR_NUMBER_WITH_REG:
                    regTo = checkGPR(memory.load(++ip));
                    number = memory.load(++ip);
                    gpr[regTo] = checkOperation(gpr[regTo] ^ number);
                    ip++;
                    break;
                case Opcode.NOT_REG:
                    regTo = checkGPR(memory.load(++ip));
                    gpr[regTo] = checkOperation(~gpr[regTo]);
                    ip++;
                    break;
                case Opcode.SHL_REG_WITH_REG:
                    regTo = checkGPR(memory.load(++ip));
                    regFrom = checkGPR(memory.load(++ip));
                    gpr[regTo] = checkOperation(gpr[regTo] << gpr[regFrom]);
                    ip++;
                    break;
                case Opcode.SHL_REGADDRESS_WITH_REG:
                    regTo = checkGPR(memory.load(++ip));
                    regFrom = memory.load(++ip);
                    gpr[regTo] = checkOperation(gpr[regTo] << memory.load(indirectRegisterAddress(regFrom)));
                    ip++;
                    break;
                case Opcode.SHL_ADDRESS_WITH_REG:
                    regTo = checkGPR(memory.load(++ip));
                    memFrom = memory.load(++ip);
                    gpr[regTo] = checkOperation(gpr[regTo] << memory.load(memFrom));
                    ip++;
                    break;
                case Opcode.SHL_NUMBER_WITH_REG:
                    regTo = checkGPR(memory.load(++ip));
                    number = memory.load(++ip);
                    gpr[regTo] = checkOperation(gpr[regTo] << number);
                    ip++;
                    break;
                case Opcode.SHR_REG_WITH_REG:
                    regTo = checkGPR(memory.load(++ip));
                    regFrom = checkGPR(memory.load(++ip));
                    gpr[regTo] = checkOperation(gpr[regTo] >>> gpr[regFrom]);
                    ip++;
                    break;
                case Opcode.SHR_REGADDRESS_WITH_REG:
                    regTo = checkGPR(memory.load(++ip));
                    regFrom = memory.load(++ip);
                    gpr[regTo] = checkOperation(gpr[regTo] >>> memory.load(indirectRegisterAddress(regFrom)));
                    ip++;
                    break;
                case Opcode.SHR_ADDRESS_WITH_REG:
                    regTo = checkGPR(memory.load(++ip));
                    memFrom = memory.load(++ip);
                    gpr[regTo] = checkOperation(gpr[regTo] >>> memory.load(memFrom));
                    ip++;
                    break;
                case Opcode.SHR_NUMBER_WITH_REG:
                    regTo = checkGPR(memory.load(++ip));
                    number = memory.load(++ip);
                    gpr[regTo] = checkOperation(gpr[regTo] >>> number);
                    ip++;
                    break;
                default:
                    throw new RuntimeException("Invalid op code: " + instr);
            }

            return;

        } catch (Exception e) {
            fault = true;
            throw e;
        }

    }

    public void runProgram() {
        while (!halted)
            step();
    }

}