package app.cleancode.compiler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import app.cleancode.Pair;

public class SourceFile {
    public final Set<String> dependentHeaders = new HashSet<>();
    public final List<Pair<String, String>> fields = new ArrayList<>();
    public final List<SourceMethod> methods = new ArrayList<>();
}
