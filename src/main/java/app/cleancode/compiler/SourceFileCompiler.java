package app.cleancode.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import app.cleancode.NameMangler;
import app.cleancode.Pair;
import app.cleancode.ParameterHelper;
import app.cleancode.TypeHelper;

public class SourceFileCompiler {
    public static SourceFile getSourceFile(ClassNode classNode) {
        SourceFile sourceFile = new SourceFile();
        classNode.methods.forEach(method -> {
            System.out.println("Doing method " + method.name);
            List<Pair<String, String>> methodParameters = new ArrayList<>();
            List<String> methodParameterTypes = ParameterHelper.getParameterTypeNames(
                    classNode.name, method.desc, (method.access & Opcodes.ACC_STATIC) == 0);
            sourceFile.dependentHeaders.addAll(methodParameterTypes);
            int i;
            if ((method.access & Opcodes.ACC_STATIC) == 0) {
                methodParameters.add(new Pair<String, String>(
                        TypeHelper.getCStyleTypeName("L" + classNode.name + ";"), "this"));
                i = 1;
            } else {
                i = 0;
            }
            for (int j = 0; i < methodParameterTypes.size(); i++, j++) {
                methodParameters.add(
                        new Pair<>(methodParameterTypes.get(i), method.parameters.get(j).name));
            }
            SourceMethod sourceMethod = new SourceMethod(
                    TypeHelper.getCStyleTypeName(Type.getReturnType(method.desc).getDescriptor()),
                    NameMangler.mangle(classNode.name, method.name, method.desc), methodParameters);
            Map<Label, String> labelNames = new HashMap<>();
            AtomicInteger numLabels = new AtomicInteger();
            OperandStack operandStack = new OperandStack();
            Map<String, OperandStack> labelOperandStacks = new HashMap<>();
            for (AbstractInsnNode instruction : method.instructions) {
                if (instruction.getType() == LabelNode.LABEL) {
                    LabelNode labelNode = (LabelNode) instruction;
                    String labelName =
                            getOrCreateLabelName(numLabels, labelNames, labelNode.getLabel());
                    sourceMethod.actions.add(new LabelAction(labelName));
                    OperandStack labelOperandStack =
                            getOrCreateOperandStack(labelName, labelOperandStacks);
                    if (!operandStack.isEmpty()) {
                        labelOperandStack.addPossibility(labelOperandStack);
                    }
                    operandStack = labelOperandStack;
                } else if (instruction.getType() == LineNumberNode.LINE) {
                    LineNumberNode lineNumber = (LineNumberNode) instruction;
                    sourceMethod.actions.add(new LineNumberAction(lineNumber.line));
                } else {
                    switch (instruction.getOpcode()) {
                        case Opcodes.GOTO: {
                            JumpInsnNode gotoInstruction = (JumpInsnNode) instruction;
                            Label target = gotoInstruction.label.getLabel();
                            String labelName = getOrCreateLabelName(numLabels, labelNames, target);
                            sourceMethod.actions.add(new GotoAction(labelNames.get(target)));
                            getOrCreateOperandStack(labelName, labelOperandStacks)
                                    .addPossibility(operandStack.possibilityCopy());
                            break;
                        }
                        case Opcodes.LDC: {
                            LdcInsnNode ldcInstruction = (LdcInsnNode) instruction;
                            List<String> ldcOutputs = new ArrayList<>();
                            sourceMethod.actions
                                    .add(new LoadConstantAction(ldcInstruction.cst, ldcOutputs));
                            operandStack.push(ldcOutputs);
                            break;
                        }
                        case Opcodes.PUTSTATIC: {
                            FieldInsnNode fieldInstruction = (FieldInsnNode) instruction;
                            sourceFile.dependentHeaders
                                    .add(TypeHelper.getActualType(fieldInstruction.desc));
                            String sourceVariable = "tmp" + sourceMethod.variables.size();
                            sourceMethod.actions.add(new PutAction(sourceVariable, NameMangler
                                    .mangle(classNode.name, method.name, fieldInstruction.name)));
                            operandStack.pop(sourceVariable);
                            sourceMethod.variables.add(
                                    new Pair<>(TypeHelper.getCStyleTypeName(fieldInstruction.desc),
                                            sourceVariable));
                            break;
                        }
                        case Opcodes.RETURN: {
                            sourceMethod.actions.add(new ArglessReturnAction());
                            break;
                        }
                        default:
                            throw new IllegalArgumentException(
                                    String.format("Unknown opcode %d of instruction type %s",
                                            instruction.getOpcode(),
                                            instruction.getClass().getSimpleName()));
                    }
                }
            }
            System.out.println(sourceMethod);
            sourceFile.methods.add(sourceMethod);
        });
        return sourceFile;
    }

    private static String getOrCreateLabelName(AtomicInteger numLabels,
            Map<Label, String> labelNames, Label label) {
        if (labelNames.containsKey(label)) {
            return labelNames.get(label);
        } else {
            String labelName = "l" + numLabels.getAndIncrement();
            labelNames.put(label, labelName);
            return labelName;
        }
    }

    private static OperandStack getOrCreateOperandStack(String labelName,
            Map<String, OperandStack> labelOperandStacks) {
        if (labelOperandStacks.containsKey(labelName)) {
            return labelOperandStacks.get(labelName);
        } else {
            OperandStack operandStack = new OperandStack();
            labelOperandStacks.put(labelName, operandStack);
            return operandStack;
        }
    }

    public static String getVariableType(Object obj) {
        if (obj instanceof Integer) {
            return "int32_t";
        } else if (obj instanceof Double) {
            return "double";
        } else if (obj instanceof Float) {
            return "float";
        } else if (obj instanceof Long) {
            return "int64_t";
        } else if (obj instanceof Short) {
            return "int16_t";
        } else if (obj instanceof Boolean) {
            return "bool";
        } else if (obj instanceof Character || obj instanceof Byte) {
            return "int8_t";
        } else {
            return TypeHelper.getCStyleTypeName(obj.getClass().descriptorString());
        }
    }
}
