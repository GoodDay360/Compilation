import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java Main <configuration-file>");
            System.exit(2);
            return;
        }

        Result result;
        try {
            result = scan(args[0]);
        } catch (NoSuchFileException exception) {
            System.err.println("Error: File not found: " + Paths.get(args[0]).toAbsolutePath());
            System.exit(1);
            return;
        } catch (IOException exception) {
            System.err.println("Error: Could not read " + args[0] + ": " + exception.getMessage());
            System.exit(1);
            return;
        }
        System.out.print(result.output);
        System.err.print(result.errors);
    }

    public static Result scan(String file) throws IOException {
        try (Reader reader = Files.newBufferedReader(Paths.get(file), StandardCharsets.UTF_8)) {
            Lexer lexer = new Lexer(reader);
            lexer.yylex();
            return new Result(lexer.getOutput(), lexer.getErrors());
        }
    }


    public static final class Result {
        public final String output;
        public final String errors;

        private Result(String output, String errors) {
            this.output = output;
            this.errors = errors;
        }
    }
}