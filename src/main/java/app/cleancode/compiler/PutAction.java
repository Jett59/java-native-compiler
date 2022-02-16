package app.cleancode.compiler;

public record PutAction(String source, String dest) implements Action {

    @Override
    public String getCCode() {
        return String.format("%s = %s;", dest, source);
    }

}
