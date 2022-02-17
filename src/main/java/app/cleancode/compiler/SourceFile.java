package app.cleancode.compiler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import app.cleancode.Pair;

public class SourceFile {
    private final String fileName;
    public final Set<String> dependentHeaders = new HashSet<>();
    public final List<Pair<String, String>> fields = new ArrayList<>();
    public final List<SourceMethod> methods = new ArrayList<>();

    public SourceFile(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        dependentHeaders.removeAll(List.of("V", "B", "C", "F", "D", "I", "J", "S", "Z"));
        StringBuilder result = new StringBuilder();
        if (fileName != null) {
            result.append("#file \"").append(fileName).append("\"\n");
        }
        result.append("#include <stdint.h>\n");
        result.append("#include <stdbool.h>\n");
        dependentHeaders
                .forEach(header -> result.append("#include \"").append(header).append(".h\"\n"));
        methods.forEach(method -> result.append(method).append('\n'));
        return result.toString();
    }
}
