package app.cleancode;

import java.util.List;

public record MethodDescriptor(String returnType, String name, List<String> parameterTypes) {

}
