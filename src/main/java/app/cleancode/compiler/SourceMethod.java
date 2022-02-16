package app.cleancode.compiler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import app.cleancode.Pair;

public class SourceMethod {
    public final String returnType;
    public final String name;
    public final List<Pair<String, String>> parameters;
    public final List<Pair<String, String>> variables = new ArrayList<>();
    public final List<Action> actions = new ArrayList<>();

    public SourceMethod(String returnType, String name, List<Pair<String, String>> parameters) {
        this.returnType = returnType;
        this.name = name;
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(returnType).append(' ').append(name).append("(")
                .append(parameters.stream().map(pair -> pair.a().concat(" ".concat(pair.b())))
                        .collect(Collectors.joining(", ")))
                .append(") {\n");
        for (Pair<String, String> variable : variables) {
            result.append(variable.a()).append(' ').append(variable.b()).append(";\n");
        }
        for (Action action : actions) {
            result.append(action.getCCode()).append('\n');
        }
        result.append('}');
        return result.toString();
    }
}
