package app.cleancode;

public class NameMangler {
    public static String mangle(String name) {
        return name.replace('/', '_').replace('.', '_');
    }

    public static String mangle(String className, String name, String type) {
        return mangle(className) + "__" + mangle(name) + "_" + mangle(type);
    }
}
