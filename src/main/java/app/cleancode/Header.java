package app.cleancode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

public record Header(String className, List<Pair<String, String>> instanceFields,
        List<Pair<String, String>> staticFields, List<MethodDescriptor> methods) {
    public static Header from(ClassNode classNode) {
        List<Pair<String, String>> instanceFields = new ArrayList<>(),
                staticFields = new ArrayList<>();
        classNode.fields.forEach(field -> {
            Pair<String, String> fieldDescriptor = new Pair<>(field.desc, field.name);
            if ((field.access & Opcodes.ACC_STATIC) == 0) { // Instance field
                instanceFields.add(fieldDescriptor);
            } else {
                staticFields.add(fieldDescriptor);
            }
        });
        List<MethodDescriptor> methods = new ArrayList<>();
        classNode.methods.forEach(method -> {
            MethodDescriptor methodDescriptor =
                    new MethodDescriptor(Type.getReturnType(method.desc).getDescriptor(),
                            method.name, Arrays.stream(Type.getArgumentTypes(method.desc))
                                    .map(type -> type.getDescriptor()).toList());
            methods.add(methodDescriptor);
        });
        return new Header(classNode.name, instanceFields, staticFields, methods);
    }
}
