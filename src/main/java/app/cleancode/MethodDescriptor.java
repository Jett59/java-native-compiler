package app.cleancode;

import java.util.List;

public record MethodDescriptor(String returnType, String name, List<String> parameterTypes) {
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(returnType).append(" ").append(name).append("(");
        result.append(String.join(", ", parameterTypes));
        result.append(")");
        return result.toString();
    }
}
