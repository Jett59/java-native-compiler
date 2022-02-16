package app.cleancode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.objectweb.asm.Type;

public class ParameterHelper {
    public static List<String> getParameterTypeNames(String className, String methodDescriptor,
            boolean instanceMethod) {
        List<String> parameterTuypeNames =
                new ArrayList<>(Arrays.stream(Type.getArgumentTypes(methodDescriptor))
                        .map(Type::getDescriptor).map(TypeHelper::getCStyleTypeName).toList());
        if (instanceMethod) {
            parameterTuypeNames.add(0, TypeHelper.getCStyleTypeName('L' + className + ';'));
        }
        return parameterTuypeNames;
    }
}
