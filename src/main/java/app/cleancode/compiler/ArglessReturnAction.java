package app.cleancode.compiler;

public record ArglessReturnAction() implements Action {

    @Override
    public String getCCode() {
        return "return;";
    }
}
