package app.cleancode.compiler;

import java.util.List;
import java.util.stream.Collectors;

public record LoadLocalAction(String localVariable, List<String> outputs) implements Action {

    @Override
    public String getCCode() {
        return outputs.stream().map(output -> String.format("%s = %s;", output, localVariable))
                .collect(Collectors.joining("\n"));
    }

}
