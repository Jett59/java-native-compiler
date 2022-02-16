package app.cleancode.compiler;

public record GotoAction(String destinationLabel) implements Action {

    @Override
    public String getCCode() {
        return String.format("goto %s;", destinationLabel);
    }
}
