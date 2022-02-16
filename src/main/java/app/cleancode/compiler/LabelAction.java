package app.cleancode.compiler;

public record LabelAction(String labelName) implements Action {

    @Override
    public String getCCode() {
        return String.format("%s:", labelName);
    }

}
