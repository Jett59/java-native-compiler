package app.cleancode.compiler;

import java.util.List;

public record CallAction(String method, List<String> inputVariables, List<String> outputVariables)
        implements Action {

    @Override
    public String getCCode() {
        String callString = method + "(" + String.join(", ", inputVariables) + ");";
        if (outputVariables.size() == 0) {
            return callString;
        } else {
            String firstVariable = outputVariables.get(0);
            StringBuilder result = new StringBuilder();
            result.append(firstVariable).append(" = ").append(callString);
            for (int i = 1; i < outputVariables.size(); i++) {
                result.append('\n').append(outputVariables.get(i)).append(" = ")
                        .append(firstVariable).append(';');
            }
            return result.toString();
        }
    }
}
