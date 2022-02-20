package app.cleancode.compiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import app.cleancode.NameMangler;
import app.cleancode.Pair;
import app.cleancode.ParameterHelper;
import app.cleancode.TypeHelper;

public class SourceFileCompiler {
    public static SourceFile getSourceFile(ClassNode classNode) {
        SourceFile sourceFile = new SourceFile(classNode.sourceFile);
        sourceFile.dependentHeaders.add(classNode.name);
        classNode.methods.forEach(method -> {
            List<LocalVariableNode> localVariables =
                    method.localVariables == null ? new ArrayList<>() : method.localVariables;
            List<Pair<String, String>> methodParameters = new ArrayList<>();
            List<String> methodParameterTypes = ParameterHelper.getParameterTypeNames(
                    classNode.name, method.desc, (method.access & Opcodes.ACC_STATIC) == 0);
            sourceFile.dependentHeaders.addAll(Arrays.stream(Type.getArgumentTypes(method.desc))
                    .map(type -> type.getInternalName()).map(TypeHelper::getActualType).toList());
            for (int i = 0; i < methodParameterTypes.size(); i++) {
                methodParameters
                        .add(new Pair<>(methodParameterTypes.get(i), localVariables.get(i).name));
            }
            SourceMethod sourceMethod = new SourceMethod(
                    TypeHelper.getCStyleTypeName(Type.getReturnType(method.desc).getDescriptor()),
                    NameMangler.mangle(classNode.name, method.name, method.desc), methodParameters);
            Map<Label, String> labelNames = new HashMap<>();
            for (int i = methodParameterTypes.size(); i < localVariables.size(); i++) {
                LocalVariableNode localVariable = localVariables.get(i);
                sourceMethod.variables.add(new Pair<String, String>(
                        TypeHelper.getCStyleTypeName(localVariable.desc), localVariable.name));
            }
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
                } else if (instruction.getType() == FrameNode.FRAME) {
                    // Do nothing for now
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
                            operandStack.push(ldcOutputs, getVariableType(ldcInstruction.cst));
                            break;
                        }
                        case Opcodes.ICONST_M1: {
                            List<String> outputs = new ArrayList<>();
                            sourceMethod.actions.add(new LoadConstantAction(-1, outputs));
                            operandStack.push(outputs, "int32_t");
                            break;
                        }
                        case Opcodes.ICONST_0: {
                            List<String> outputs = new ArrayList<>();
                            sourceMethod.actions.add(new LoadConstantAction(0, outputs));
                            operandStack.push(outputs, "int32_t");
                            break;
                        }
                        case Opcodes.ICONST_1: {
                            List<String> outputs = new ArrayList<>();
                            sourceMethod.actions.add(new LoadConstantAction(1, outputs));
                            operandStack.push(outputs, "int32_t");
                            break;
                        }
                        case Opcodes.ICONST_2: {
                            List<String> outputs = new ArrayList<>();
                            sourceMethod.actions.add(new LoadConstantAction(2, outputs));
                            operandStack.push(outputs, "int32_t");
                            break;
                        }
                        case Opcodes.ICONST_3: {
                            List<String> outputs = new ArrayList<>();
                            sourceMethod.actions.add(new LoadConstantAction(3, outputs));
                            operandStack.push(outputs, "int32_t");
                            break;
                        }
                        case Opcodes.ICONST_4: {
                            List<String> outputs = new ArrayList<>();
                            sourceMethod.actions.add(new LoadConstantAction(4, outputs));
                            operandStack.push(outputs, "int32_t");
                            break;
                        }
                        case Opcodes.ICONST_5: {
                            List<String> outputs = new ArrayList<>();
                            sourceMethod.actions.add(new LoadConstantAction(5, outputs));
                            operandStack.push(outputs, "int32_t");
                            break;
                        }
                        case Opcodes.PUTSTATIC: {
                            FieldInsnNode fieldInstruction = (FieldInsnNode) instruction;
                            sourceFile.dependentHeaders.add(fieldInstruction.owner);
                            String sourceVariable = getTmpVariableName(sourceMethod.variables);
                            sourceMethod.actions.add(
                                    new PutAction(sourceVariable, NameMangler.mangle(classNode.name,
                                            fieldInstruction.name, fieldInstruction.desc)));
                            operandStack.pop(sourceVariable);
                            sourceMethod.variables.add(
                                    new Pair<>(TypeHelper.getCStyleTypeName(fieldInstruction.desc),
                                            sourceVariable));
                            sourceFile.dependentHeaders
                                    .add(TypeHelper.getActualType(fieldInstruction.desc));
                            break;
                        }
                        case Opcodes.RETURN: {
                            sourceMethod.actions.add(new ArglessReturnAction());
                            break;
                        }
                        case Opcodes.IRETURN: {
                            String returnVariable = getTmpVariableName(sourceMethod.variables);
                            sourceMethod.actions.add(new ReturnAction(returnVariable));
                            operandStack.pop(returnVariable);
                            sourceMethod.variables
                                    .add(new Pair<String, String>("int32_t", returnVariable));
                            break;
                        }
                        case Opcodes.ASTORE: {
                            VarInsnNode variableInstruction = (VarInsnNode) instruction;
                            String inputVariable = getTmpVariableName(sourceMethod.variables);
                            sourceMethod.actions.add(new StoreAction(inputVariable,
                                    List.of(localVariables.get(variableInstruction.var).name)));
                            String inputVariableType = operandStack.pop(inputVariable);
                            sourceMethod.variables.add(
                                    new Pair<String, String>(inputVariableType, inputVariable));
                            break;
                        }
                        case Opcodes.ALOAD: {
                            VarInsnNode variableInstruction = (VarInsnNode) instruction;
                            List<String> outputs = new ArrayList<>();
                            sourceMethod.actions.add(new LoadLocalAction(
                                    localVariables.get(variableInstruction.var).name, outputs));
                            operandStack.push(outputs, TypeHelper.getCStyleTypeName(
                                    localVariables.get(variableInstruction.var).desc));
                            break;
                        }
                        case Opcodes.INVOKESPECIAL: {
                            MethodInsnNode methodInstruction = (MethodInsnNode) instruction;
                            Arrays.stream(Type.getArgumentTypes(methodInstruction.desc))
                                    .map(Type::getDescriptor).map(TypeHelper::getActualType)
                                    .forEach(type -> sourceFile.dependentHeaders.add(type));
                            sourceFile.dependentHeaders.add(methodInstruction.owner);
                            List<String> parameterTypes = ParameterHelper.getParameterTypeNames(
                                    methodInstruction.owner, methodInstruction.desc,
                                    (method.access & Opcodes.ACC_STATIC) == 0);
                            List<String> outputs = new ArrayList<>();
                            List<Pair<String, String>> inputVariables =
                                    IntStream.range(0, parameterTypes.size())
                                            .mapToObj(n -> new Pair<>(parameterTypes.get(n),
                                                    getTmpVariableName(sourceMethod.variables)))
                                            .toList();
                            sourceMethod.actions.add(new CallAction(
                                    NameMangler.mangle(methodInstruction.owner,
                                            methodInstruction.name, methodInstruction.desc),
                                    inputVariables.stream().map(Pair::b).toList(), outputs));
                            sourceMethod.variables.addAll(inputVariables);
                            for (int j = inputVariables.size() - 1; j >= 0; j--) {
                                operandStack.pop(inputVariables.get(j).b());
                            }
                            if (Type.getReturnType(methodInstruction.desc).getSort() != Type.VOID) {
                                operandStack.push(outputs, TypeHelper.getCStyleTypeName(Type
                                        .getReturnType(methodInstruction.desc).getDescriptor()));
                            }
                            break;
                        }
                        case Opcodes.ISTORE: {
                            VarInsnNode variableInstruction = (VarInsnNode) instruction;
                            String inputVariable = getTmpVariableName(sourceMethod.variables);
                            sourceMethod.actions.add(new StoreAction(inputVariable,
                                    List.of(localVariables.get(variableInstruction.var).name)));
                            sourceMethod.variables
                                    .add(new Pair<String, String>("int8_t", inputVariable));
                            operandStack.pop(inputVariable);
                            break;
                        }
                        case Opcodes.ILOAD: {
                            VarInsnNode variableInstruction = (VarInsnNode) instruction;
                            List<String> outputs = new ArrayList<>();
                            sourceMethod.actions.add(new StoreAction(
                                    localVariables.get(variableInstruction.var).name, outputs));
                            operandStack.push(outputs, TypeHelper.getCStyleTypeName(
                                    localVariables.get(variableInstruction.var).desc));
                            break;
                        }
                        case Opcodes.CALOAD:
                        case Opcodes.AALOAD: {
                            String arrayVariable =
                                    getTmpVariableName(sourceMethod.variables) + "_array";
                            String indexVariable =
                                    getTmpVariableName(sourceMethod.variables) + "_index";
                            List<String> outputs = new ArrayList<>();
                            sourceMethod.actions.add(
                                    new ArrayIndexAction(arrayVariable, indexVariable, outputs));
                            String indexType = operandStack.pop(indexVariable);
                            String arrayType = operandStack.pop(arrayVariable);
                            operandStack.push(outputs, TypeHelper.removeDimensionCStyle(arrayType));
                            sourceMethod.variables
                                    .add(new Pair<String, String>(arrayType, arrayVariable));
                            sourceMethod.variables
                                    .add(new Pair<String, String>(indexType, indexVariable));
                            break;
                        }
                        case Opcodes.IFNE: {
                            JumpInsnNode jumpInstruction = (JumpInsnNode) instruction;
                            String inputVariable = getTmpVariableName(sourceMethod.variables);
                            sourceMethod.actions.add(new CompareAction(inputVariable, "!=", "0",
                                    getOrCreateLabelName(numLabels, labelNames,
                                            jumpInstruction.label.getLabel())));
                            String inputVariableType = operandStack.pop(inputVariable);
                            sourceMethod.variables.add(
                                    new Pair<String, String>(inputVariableType, inputVariable));
                            break;
                        }
                        case Opcodes.IF_ICMPLT: {
                            JumpInsnNode jumpInstruction = (JumpInsnNode) instruction;
                            String variableA = getTmpVariableName(sourceMethod.variables) + "_a";
                            String variableB = getTmpVariableName(sourceMethod.variables) + "_b";
                            sourceMethod.actions.add(new CompareAction(variableA, "<", variableB,
                                    getOrCreateLabelName(numLabels, labelNames,
                                            jumpInstruction.label.getLabel())));
                            labelOperandStacks.get(labelNames.get(jumpInstruction.label.getLabel()))
                                    .addPossibility(operandStack.possibilityCopy());
                            String variableBType = operandStack.pop(variableB);
                            String variableAType = operandStack.pop(variableA);
                            sourceMethod.variables
                                    .add(new Pair<String, String>(variableAType, variableA));
                            sourceMethod.variables
                                    .add(new Pair<String, String>(variableBType, variableB));
                            break;
                        }
                        case Opcodes.IF_ICMPGE: {
                            JumpInsnNode jumpInstruction = (JumpInsnNode) instruction;
                            String variableA = getTmpVariableName(sourceMethod.variables) + "_a";
                            String variableB = getTmpVariableName(sourceMethod.variables) + "_b";
                            sourceMethod.actions.add(new CompareAction(variableA, ">=", variableB,
                                    getOrCreateLabelName(numLabels, labelNames,
                                            jumpInstruction.label.getLabel())));
                            labelOperandStacks.get(labelNames.get(jumpInstruction.label.getLabel()))
                                    .addPossibility(operandStack.possibilityCopy());
                            String variableBType = operandStack.pop(variableB);
                            String variableAType = operandStack.pop(variableA);
                            sourceMethod.variables
                                    .add(new Pair<String, String>(variableAType, variableA));
                            sourceMethod.variables
                                    .add(new Pair<String, String>(variableBType, variableB));
                            break;
                        }
                        case Opcodes.IINC: {
                            IincInsnNode incrementInstruction = (IincInsnNode) instruction;
                            sourceMethod.actions.add(new TransformAction(
                                    localVariables.get(incrementInstruction.var).name, "+",
                                    Integer.toString(incrementInstruction.incr)));
                            break;
                        }
                        default:
                            System.out.println(sourceMethod);
                            throw new IllegalArgumentException(
                                    String.format("Unknown opcode %d of instruction type %s",
                                            instruction.getOpcode(),
                                            instruction.getClass().getSimpleName()));
                    }
                }
            }
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

    private static String getTmpVariableName(List<Pair<String, String>> variables) {
        return "_internal_tmp_" + variables.size();
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
