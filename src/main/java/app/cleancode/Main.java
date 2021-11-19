package app.cleancode;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

public class Main {
    public static String className = Main.class.getCanonicalName();

    public static void main(String[] args) throws Throwable {
        ClassNode root = new ClassNode();
        ClassReader reader = new ClassReader(Main.class.getName());
        reader.accept(root, 0);
        Header header = Header.from(root);
        System.out.println(header);
    }
}
