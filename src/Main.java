import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) throws IOException {
        // The program expects exactly one command-line argument: the configuration file to scan.
        if (args.length != 1) {
            System.err.println("Usage: java Main <configuration-file>");
            System.exit(2);
            return;
        }

        // Open the file as UTF-8 so non-ASCII text is read correctly. The try block closes
        // the file when scanning finishes, including if reading or scanning reports an error.
        try (Reader reader = Files.newBufferedReader(Paths.get(args[0]), StandardCharsets.UTF_8)) {
            // Lexer reads characters from the file and prints each recognized token and its position.
            new Lexer(reader).yylex();
        }
    }
}