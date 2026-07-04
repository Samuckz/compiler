package codegen;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

public class CodeGenerator {

    // Slot 0 = args (String[]), slot 1 = Scanner instance
    public static final int SCANNER_SLOT = 1;
    private static final int FIRST_VAR_SLOT = 2;

    private final StringBuilder header = new StringBuilder();
    private final StringBuilder body   = new StringBuilder();

    private int labelCount = 0;
    private int nextSlot   = FIRST_VAR_SLOT;

    private final Map<String, Integer> varSlots = new LinkedHashMap<>();
    private final Map<String, String>  varTypes = new LinkedHashMap<>();

    private String outputFilename;

    // -------------------------------------------------------------------------
    // Variable management
    // -------------------------------------------------------------------------

    public void allocVar(String name, String type) {
        if (!varSlots.containsKey(name)) {
            varSlots.put(name, nextSlot++);
            varTypes.put(name, type);
        }
    }

    public int getSlot(String name) {
        return varSlots.getOrDefault(name, -1);
    }

    public String getVarType(String name) {
        return varTypes.getOrDefault(name, "error");
    }

    // -------------------------------------------------------------------------
    // Label generation
    // -------------------------------------------------------------------------

    public String newLabel() {
        return "L" + (labelCount++);
    }

    // -------------------------------------------------------------------------
    // Emission primitives
    // -------------------------------------------------------------------------

    public void emit(String instruction) {
        body.append("    ").append(instruction).append("\n");
    }

    public void emitLabel(String label) {
        body.append(label).append(":\n");
    }

    // -------------------------------------------------------------------------
    // Header / footer
    // -------------------------------------------------------------------------

    public void emitHeader(String className) {
        outputFilename = className + ".j";

        header.append(".class public ").append(className).append("\n");
        header.append(".super java/lang/Object\n\n");
        header.append(".method public static main([Ljava/lang/String;)V\n");
        header.append("    .limit stack 20\n");
        header.append("    .limit locals ").append(nextSlot + 5).append("\n\n");

        // Initialize Scanner(System.in) and store in slot 1
        header.append("    new java/util/Scanner\n");
        header.append("    dup\n");
        header.append("    getstatic java/lang/System/in Ljava/io/InputStream;\n");
        header.append("    invokespecial java/util/Scanner/<init>(Ljava/io/InputStream;)V\n");
        header.append("    astore ").append(SCANNER_SLOT).append("\n\n");
    }

    public void emitFooter() {
        body.append("\n    return\n");
        body.append(".end method\n");
    }

    // -------------------------------------------------------------------------
    // Load / store
    // -------------------------------------------------------------------------

    public void emitLoad(String varName, String type) {
        int slot = getSlot(varName);
        switch (type) {
            case "int":   emit("iload " + slot); break;
            case "float": emit("fload " + slot); break;
            default:      emit("aload " + slot); break;
        }
    }

    public void emitStore(String varName, String type) {
        int slot = getSlot(varName);
        switch (type) {
            case "int":   emit("istore " + slot); break;
            case "float": emit("fstore " + slot); break;
            default:      emit("astore " + slot); break;
        }
    }

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    public void emitIntConst(int value) {
        if (value >= 0 && value <= 5) {
            emit("iconst_" + value);
        } else if (value == -1) {
            emit("iconst_m1");
        } else if (value >= -128 && value <= 127) {
            emit("bipush " + value);
        } else if (value >= -32768 && value <= 32767) {
            emit("sipush " + value);
        } else {
            emit("ldc " + value);
        }
    }

    public void emitFloatConst(double value) {
        // Jasmin treats floating-point ldc as float when stored in float slot
        emit("ldc " + value);
    }

    public void emitStringConst(String value) {
        // value already contains the raw string content (without surrounding quotes)
        emit("ldc \"" + escape(value) + "\"");
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    // -------------------------------------------------------------------------
    // Arithmetic
    // -------------------------------------------------------------------------

    public void emitAdd(String type) {
        switch (type) {
            case "int":   emit("iadd"); break;
            case "float": emit("fadd"); break;
            // string concatenation
            default: emit("invokevirtual java/lang/String/concat(Ljava/lang/String;)Ljava/lang/String;"); break;
        }
    }

    public void emitSub(String type) {
        switch (type) {
            case "int":   emit("isub"); break;
            case "float": emit("fsub"); break;
        }
    }

    public void emitMul(String type) {
        switch (type) {
            case "int":   emit("imul"); break;
            case "float": emit("fmul"); break;
        }
    }

    // int / int → float (per language spec)
    public void emitDiv(String leftType, String rightType) {
        if (leftType.equals("int") && rightType.equals("int")) {
            // stack: [..., left_int, right_int]
            emit("swap");
            emit("i2f");
            emit("swap");
            emit("i2f");
            emit("fdiv");
        } else if (leftType.equals("int")) {
            // stack: [..., left_int, right_float]
            emit("swap");
            emit("i2f");
            emit("swap");
            emit("fdiv");
        } else if (rightType.equals("int")) {
            // stack: [..., left_float, right_int]
            emit("i2f");
            emit("fdiv");
        } else {
            emit("fdiv");
        }
    }

    public void emitMod() { emit("irem"); }

    public void emitNeg(String type) {
        switch (type) {
            case "int":   emit("ineg"); break;
            case "float": emit("fneg"); break;
        }
    }

    // -------------------------------------------------------------------------
    // Boolean
    // -------------------------------------------------------------------------

    // not: flips 0/1 boolean using XOR with 1
    public void emitNot() {
        emit("iconst_1");
        emit("ixor");
    }

    public void emitAnd() { emit("iand"); }
    public void emitOr()  { emit("ior");  }

    // -------------------------------------------------------------------------
    // Relational operators — leaves 0 (false) or 1 (true) on stack
    // -------------------------------------------------------------------------

    public void emitRelop(String op, String operandType) {
        String lTrue  = newLabel();
        String lAfter = newLabel();

        if (operandType.equals("float")) {
            // fcmpg: pops b then a, pushes 1 if a>b, 0 if a==b, -1 if a<b
            boolean useG = op.equals(">") || op.equals(">=") || op.equals("<>");
            emit(useG ? "fcmpg" : "fcmpl");
            emit(floatBranch(op) + " " + lTrue);
        } else if (operandType.equals("string") && op.equals("=")) {
            emit("invokevirtual java/lang/String/equals(Ljava/lang/Object;)Z");
            emit("ifne " + lTrue);
        } else {
            emit(intBranch(op) + " " + lTrue);
        }

        emit("iconst_0");
        emit("goto " + lAfter);
        emitLabel(lTrue);
        emit("iconst_1");
        emitLabel(lAfter);
    }

    private String intBranch(String op) {
        switch (op) {
            case ">":  return "if_icmpgt";
            case ">=": return "if_icmpge";
            case "<":  return "if_icmplt";
            case "<=": return "if_icmple";
            case "=":  return "if_icmpeq";
            case "<>": return "if_icmpne";
            default:   return "if_icmpeq";
        }
    }

    private String floatBranch(String op) {
        switch (op) {
            case ">":  return "ifgt";
            case ">=": return "ifge";
            case "<":  return "iflt";
            case "<=": return "ifle";
            case "=":  return "ifeq";
            case "<>": return "ifne";
            default:   return "ifeq";
        }
    }

    // -------------------------------------------------------------------------
    // I/O
    // -------------------------------------------------------------------------

    public void emitRead(String varName, String type) {
        emit("aload " + SCANNER_SLOT);
        switch (type) {
            case "int":
                emit("invokevirtual java/util/Scanner/nextInt()I");
                emit("istore " + getSlot(varName));
                break;
            case "float":
                emit("invokevirtual java/util/Scanner/nextFloat()F");
                emit("fstore " + getSlot(varName));
                break;
            default: // string
                emit("invokevirtual java/util/Scanner/next()Ljava/lang/String;");
                emit("astore " + getSlot(varName));
                break;
        }
    }

    public void emitPrintln(String type) {
        switch (type) {
            case "int":
            case "bool":
                emit("invokevirtual java/io/PrintStream/println(I)V");
                break;
            case "float":
                emit("invokevirtual java/io/PrintStream/println(F)V");
                break;
            default: // string
                emit("invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V");
                break;
        }
    }

    // -------------------------------------------------------------------------
    // Output
    // -------------------------------------------------------------------------

    public String getOutput() {
        return header.toString() + body.toString();
    }

    public String getOutputFilename() {
        return outputFilename;
    }

    public void saveToFile(String filename) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.print(getOutput());
        }
    }
}
