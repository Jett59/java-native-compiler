package app.cleancode.compiler;

public record LineNumberAction(int lineNumber) implements Action {

    @Override
    public String getCCode() {
        return String.format("#line %d", lineNumber);
    }
}
