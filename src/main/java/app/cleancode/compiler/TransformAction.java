package app.cleancode.compiler;

public record TransformAction(String variable, String operator, String operand) implements Action {

    @Override
    public String getCCode() {
        return String.format("%s %s= %s;", variable, operator, operand);
    }
}
