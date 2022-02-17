package app.cleancode.compiler;

public record CompareAction(String input, String operator, String other, String target)
        implements Action {

    @Override
    public String getCCode() {
        return String.format("if (%s %s %s)\ngoto %s;", input, operator, other, target);
    }
}
