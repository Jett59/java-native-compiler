package app.cleancode;

public class Test {
    public static int main(int argc, char[][] argv) {
        for (int i = 0; i < argc; i++) {
            char[] arg = argv[i];
            if (arg[0] == 0) {
                return i;
            }
        }
        return 0;
    }
}
