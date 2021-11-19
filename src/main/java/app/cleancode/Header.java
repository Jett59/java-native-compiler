package app.cleancode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

public record Header(String className, List<Pair<String, String>> instanceFields,
        List<Pair<String, String>> staticFields, List<MethodDescriptor> methods,
        Set<String> dependentHeaders) {
    public static Header from(ClassNode classNode) {
        Set<String> dependentHeaders = new HashSet<>();
        List<Pair<String, String>> instanceFields = new ArrayList<>(),
                staticFields = new ArrayList<>();
        classNode.fields.forEach(field -> {
            Pair<String, String> fieldDescriptor =
                    new Pair<>(TypeHelper.getCStyleTypeName(field.desc),
                            NameMangler.mangle(classNode.name, field.name, field.desc));
            if ((field.access & Opcodes.ACC_STATIC) == 0) { // Instance field
                instanceFields.add(fieldDescriptor);
            } else {
                staticFields.add(fieldDescriptor);
            }
            dependentHeaders.add(TypeHelper.getActualType(field.desc));
        });
        List<MethodDescriptor> methods = new ArrayList<>();
        classNode.methods.forEach(method -> {
            List<String> parameterTuypeNames =
                    new ArrayList<>(Arrays.stream(Type.getArgumentTypes(method.desc))
                            .map(Type::getDescriptor).map(TypeHelper::getCStyleTypeName).toList());
            if ((method.access & Opcodes.ACC_STATIC) == 0) {
                parameterTuypeNames.add(0,
                        TypeHelper.getCStyleTypeName('L' + classNode.name + ';'));
            }
            MethodDescriptor methodDescriptor = new MethodDescriptor(
                    TypeHelper.getCStyleTypeName(Type.getReturnType(method.desc).getDescriptor()),
                    NameMangler.mangle(classNode.name, method.name, method.desc),
                    parameterTuypeNames);
            methods.add(methodDescriptor);
            dependentHeaders
                    .add(TypeHelper.getActualType(Type.getReturnType(method.desc).getDescriptor()));
            Arrays.stream(Type.getArgumentTypes(method.desc)).map(Type::getDescriptor)
                    .map(TypeHelper::getActualType).forEach(dependentHeaders::add);
        });
        dependentHeaders.removeAll(List.of("V", "B", "C", "F", "D", "I", "J", "S", "Z"));
        return new Header(NameMangler.mangle(classNode.name), instanceFields, staticFields, methods,
                dependentHeaders);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(
                "/*Compiled by the java to native compiler <https://github.com/Jett59/java-native-compiler>*/\n");
        result.append("/*Only modify if you really know what you are doing*/\n\n");
        result.append("#ifndef ___JAVA_").append(className).append("\n");
        result.append("#define ___JAVA_").append(className).append("  1\n\n");

        result.append("#include <stdint.h>\n").append("#include <stdbool.h>\n");
        dependentHeaders
                .forEach(header -> result.append("#include \"").append(header).append(".h\"\n"));

        result.append("#endif");
        return result.toString();
    }
}
