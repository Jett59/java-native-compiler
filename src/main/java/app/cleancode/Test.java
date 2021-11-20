package app.cleancode;

public class Test {
    private String str = "I am private";
    private static String staticStr = "I am private and static";
    public String publicStr = "I am public";
    public static String publicStaticStr = "I am public and static";

    public static int main(int argc, char[][] argv) {
        return 0;
    }

    private static int main(long argc, byte[][] argv) {
        return 0;
    }
}
