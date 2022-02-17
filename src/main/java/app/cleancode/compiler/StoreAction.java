package app.cleancode.compiler;

import java.util.List;
import java.util.stream.Collectors;

public record StoreAction(String input, List<String> outputs) implements Action {

    @Override
    public String getCCode() {
        return outputs.stream().map(output -> String.format("%s = %s;", output, input))
                .collect(Collectors.joining("\n"));
    }

}
