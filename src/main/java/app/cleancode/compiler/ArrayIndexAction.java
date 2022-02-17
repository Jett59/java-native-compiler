package app.cleancode.compiler;

import java.util.List;
import java.util.stream.Collectors;

public record ArrayIndexAction(String arrayVariable, String indexVariable, List<String> outputs)
        implements Action {

    @Override
    public String getCCode() {
        return outputs.stream()
                .map(output -> String.format("%s = %s[%s];", output, arrayVariable, indexVariable))
                .collect(Collectors.joining("\n"));
    }
}
