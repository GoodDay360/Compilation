import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class TestRunner {
    private TestRunner() {
    }

    public static void main(String[] args) throws IOException {
        // Use the provided case directory, or find the default from the project root or src.
        Path casesDirectory = args.length == 0 ? Paths.get("tests/cases") : Paths.get(args[0]);
        if (args.length == 0 && !Files.isDirectory(casesDirectory)) {
            casesDirectory = Paths.get("../tests/cases");
        }

        // Sort the input files so the test results always appear in the same order.
        List<Path> inputs;
        try (java.util.stream.Stream<Path> paths = Files.list(casesDirectory)) {
            inputs = paths.filter(path -> path.toString().endsWith(".cfg"))
                    .sorted(Comparator.naturalOrder())
                    .collect(Collectors.toList());
        }

        int failures = 0;
        for (Path input : inputs) {
            // Each input is compared with files sharing its name but ending in .out and .err.
            String name = input.getFileName().toString();
            name = name.substring(0, name.length() - 4);
            Main.Result result;
            try {
                result = Main.scan(input.toString());
            } catch (IOException | RuntimeException exception) {
                System.out.println("FAIL " + name + " ("
                        + exception.getClass().getSimpleName() + ": " + exception.getMessage() + ")");
                failures++;
                continue;
            }
            Path expectedOutputPath = input.resolveSibling(name + ".out");
            Path expectedErrorPath = input.resolveSibling(name + ".err");
            String expectedOutput = readIfPresent(expectedOutputPath);
            String expectedError = readIfPresent(expectedErrorPath);

            // A missing .err file means the case is expected to produce no errors.
            if (expectedOutput.equals(result.output) && expectedError.equals(result.errors)) {
                System.out.println("PASS " + name);
            } else {
                System.out.println("FAIL " + name);
                if (!expectedOutput.equals(result.output)) {
                    System.out.println("  Expected output: " + printable(expectedOutput));
                    System.out.println("  Actual output:   " + printable(result.output));
                }
                if (!expectedError.equals(result.errors)) {
                    System.out.println("  Expected errors: " + printable(expectedError));
                    System.out.println("  Actual errors:   " + printable(result.errors));
                }
                failures++;
            }
        }

        System.out.println((inputs.size() - failures) + "/" + inputs.size() + " cases passed");
        if (failures > 0) {
            // Return a failure status so scripts can detect failing tests.
            System.exit(1);
        }
    }

    private static String readIfPresent(Path path) throws IOException {
        // Treat absent expected-output files as empty. Normalize Windows line endings, since Git
        // may check out the expected files with \r\n while the lexer always writes \n.
        return Files.exists(path)
                ? Files.readString(path, StandardCharsets.UTF_8).replace("\r\n", "\n")
                : "";
    }

    private static String printable(String text) {
        // Show line breaks in one line when reporting a mismatch.
        return text.replace("\r", "\\r").replace("\n", "\\n");
    }
}
