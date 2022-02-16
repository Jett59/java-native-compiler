package app.cleancode.compiler;

import java.util.List;
import java.util.stream.Collectors;

public record LoadConstantAction(Object constant, List<String> outputVariables) implements Action {

    @Override
    public String getCCode() {
        String format;
        if (constant instanceof String) {
            format = String.format("%%s = \"%s\";", constant.toString());
        } else if (constant instanceof Integer) {
            format = String.format("%%s = %d;", (Integer) constant);
        } else {
            throw new IllegalArgumentException(
                    "Unknown constant type " + constant.getClass().getSimpleName());
        }
        return outputVariables.stream().map(output -> String.format(format, output))
                .collect(Collectors.joining("\n"));
    }
}
