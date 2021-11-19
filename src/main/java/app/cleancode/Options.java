package app.cleancode;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public record Options(String outputDirectory, List<String> inputFiles) {
    public static Options get(String[] commandLine) {
        String outputDirectory = Paths.get(".").toAbsolutePath().toString();
        List<String> inputFiles = new ArrayList<>();
        for (int i = 0; i < commandLine.length; i++) {
            String option = commandLine[i];
            if (option.equals("-o")) {
                outputDirectory = commandLine[++i];
            } else if (Files.exists(Paths.get(option))) {
                inputFiles.add(option);
            } else {
                System.err.println("Warning: unknown command line option: " + option);
            }
        }
        return new Options(outputDirectory, inputFiles);
    }
}
