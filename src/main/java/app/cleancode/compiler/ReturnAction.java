package app.cleancode.compiler;

public record ReturnAction(String returnVariable) implements Action {

    @Override
    public String getCCode() {
        return String.format("return %s;", returnVariable);
    }
}
