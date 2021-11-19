package app.cleancode;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

public class Main {
    public static String className = Main.class.getCanonicalName();
    public int[] items;

    public static void main(String[] args) throws Throwable {
        Options options = Options.get(args);
        for (String file : options.inputFiles()) {
            ClassNode root = new ClassNode();
            ClassReader reader = new ClassReader(Files.readAllBytes(Paths.get(file)));
            reader.accept(root, 0);
            Header header = Header.from(root);
            File headerOutputFile = Paths.get(options.outputDirectory(), root.name + ".h").toFile();
            headerOutputFile.getParentFile().mkdirs();
            Files.write(headerOutputFile.toPath(), header.toString().getBytes());
        }
    }
}
