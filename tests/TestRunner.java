import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
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
        // Use the provided folder, or the default folder when no argument is given.
        // Each .cfg input is checked against same-named .out and optional .err files.
        Path casesDirectory = Paths.get(args.length == 0 ? "tests/cases" : args[0]);
        List<Path> inputs;
        try (java.util.stream.Stream<Path> paths = Files.list(casesDirectory)) {
            // Sorting makes the results print in a predictable order every time.
            inputs = paths.filter(path -> path.toString().endsWith(".cfg"))
                    .sorted(Comparator.naturalOrder())
                    .collect(Collectors.toList());
        }

        int failures = 0;
        for (Path input : inputs) {
            // Remove ".cfg" to find the matching expected-output and expected-error filenames.
            String name = input.getFileName().toString();
            name = name.substring(0, name.length() - 4);
            String actualOutput;
            String actualError;
            try {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                ByteArrayOutputStream error = new ByteArrayOutputStream();
                PrintStream originalOut = System.out;
                PrintStream originalErr = System.err;
                // The lexer writes tokens to standard output and invalid-character messages to
                // standard error. Capture both streams so they can be checked against the fixtures.
                try (PrintStream capturedOut = new PrintStream(output, true, StandardCharsets.UTF_8);
                    PrintStream capturedErr = new PrintStream(error, true, StandardCharsets.UTF_8)) {
                    System.setOut(capturedOut);
                    System.setErr(capturedErr);
                    Main.main(new String[] {input.toString()});
                } finally {
                    // Restore the normal console streams even if a test throws an exception.
                    System.setOut(originalOut);
                    System.setErr(originalErr);
                }
                actualOutput = output.toString(StandardCharsets.UTF_8);
                actualError = error.toString(StandardCharsets.UTF_8);
            } catch (IOException | RuntimeException exception) {
                System.out.println("FAIL " + name + " ("
                        + exception.getClass().getSimpleName() + ": " + exception.getMessage() + ")");
                failures++;
                continue;
            }

            Path expectedOutputPath = input.resolveSibling(name + ".out");
            Path expectedErrorPath = input.resolveSibling(name + ".err");
            // Some cases have no errors, so a missing .err file means empty expected error output.
            String expectedOutput = readIfPresent(expectedOutputPath);
            String expectedError = readIfPresent(expectedErrorPath);
            if (expectedOutput.equals(actualOutput) && expectedError.equals(actualError)) {
                System.out.println("PASS " + name);
            } else {
                System.out.println("FAIL " + name);
                if (!expectedOutput.equals(actualOutput)) {
                    System.out.println("  Expected output: " + printable(expectedOutput));
                    System.out.println("  Actual output:   " + printable(actualOutput));
                }
                if (!expectedError.equals(actualError)) {
                    System.out.println("  Expected errors: " + printable(expectedError));
                    System.out.println("  Actual errors:   " + printable(actualError));
                }
                failures++;
            }
        }

        // A nonzero process exit code lets other tools know that at least one test failed.
        System.out.println((inputs.size() - failures) + "/" + inputs.size() + " cases passed");
        if (failures > 0) {
            System.exit(1);
        }
    }

    private static String readIfPresent(Path path) throws IOException {
        // Expected-output files are UTF-8 text; absent files represent no output on that stream.
        return Files.exists(path) ? Files.readString(path, StandardCharsets.UTF_8) : "";
    }

    private static String printable(String text) {
        // Show line breaks visibly so expected and actual output fit on one comparison line.
        return text.replace("\n", "\\n");
    }
}
