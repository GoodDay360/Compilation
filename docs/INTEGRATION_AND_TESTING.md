# Java Integration and Testing

This project scans configuration files using a JFlex lexer and a small Java
program. The token names and patterns are defined in
[`TOKEN_SPECIFICATION.md`](TOKEN_SPECIFICATION.md).

## How the Java program works

`src/Main.java` is the program's entry point. It expects one command-line
argument: the path to a configuration file. It opens that file using UTF-8,
passes the reader to `Lexer`, and calls `yylex()` to scan the entire file.
The reader is closed automatically after scanning.

`src/scanner.flex` contains the lexer rules, and `src/Lexer.java` is the
generated Java scanner used by the program. As the lexer reads the file, it:

- Prints each recognized token to standard output with its type, text, line,
  and column. Line and column numbers start at 1.
- Ignores whitespace and `#` comments.
- Prints unexpected-character errors to standard error and continues scanning.
- Counts supplementary Unicode characters, such as emoji, as one column.

The lexer follows JFlex's longest-match rule. For example, `true` is a
`BOOLEAN`, but `trueValue` is one `IDENTIFIER`; `30.5` is one `DECIMAL`.

## Compile and run

From the project root, change to the `src` directory first:

```text
cd src
```

From `src`, you can run the source directly on Java 11 or newer. `Main.java`
uses the `Lexer` class in the same directory, and the sample input is in the
`tests` directory one level above:

```text
java Main.java ../tests/cases/01_all_tokens.cfg
```

Alternatively, compile and run the classes explicitly:

```text
javac -encoding UTF-8 Lexer.java Main.java
java Main ../tests/cases/01_all_tokens.cfg
```

The command `java -cp out Main ...` only works if you first compile the
classes into an `out` directory. If no such compiled `Main.class` is in the
classpath, Java reports `Could not find or load main class Main`. From the
`src` directory, the simplest option is the direct source command above;
there is no need to create an `out` directory.

In either approach, replace the sample input path with the path to the
configuration file you want to scan. Since the working directory is `src`,
relative input paths are resolved from that directory. If the file argument
is missing or there is more than one argument, the program prints a usage
message.

The classpath must point to the directory containing the compiled `.class`
files. For example, `java -cp out Main ...` works only if `Main.class` and
`Lexer.class` have first been compiled into an `out` directory. The explicit
compile-and-run example above puts the class files in `src`, so it runs with
`java Main ...`.

### Where the output goes

When you run `Main`, tokens are printed to the terminal; they are not
automatically saved to a file. Token output goes to standard output, and
unexpected-character messages go to standard error.

If you want to save the token output, use `>` in the terminal command:

```text
java Main ../tests/cases/01_all_tokens.cfg > output.txt
```

This creates `output.txt` in the current directory (or replaces it if it
already exists). The `>` saves standard output only. To save errors separately,
use `2>`:

```text
java Main ../tests/cases/11_errors.cfg > output.txt 2> errors.txt
```

You can also run the source-file version and choose a different output
filename, such as `testout.txt`:

```text
java Main.java ../tests/cases/01_all_tokens.cfg > testout.txt
```

This works from `src` on Java 11 or newer. `testout.txt` is created in the
current directory (`src`) and contains the token output; it does not contain
error messages written to standard error.

These commands assume you are still in `src`; the files are saved in that
current directory.

The `.out` and `.err` files already in `tests/cases/` are expected results
used for checking; normal runs of `Main` do not create or change those files.

## Run the tests

The test inputs are in `tests/cases/`. Each `.cfg` input has a matching `.out`
file containing the expected token output. Cases that expect scanner errors
also have a matching `.err` file. If an `.err` file is absent, the expected
error output is empty.

For the tests, change back to the project root:

```text
cd ..
```

Compile and run the Java test runner from the project root:

```text
javac -encoding UTF-8 src/Lexer.java src/Main.java tests/TestRunner.java
java -cp src:tests TestRunner
```

Here, `javac` puts `Main.class` and `Lexer.class` in `src`, and `TestRunner.class`
in `tests`. The classpath `src:tests` tells Java to look in both directories.
`TestRunner` finds the `.cfg` files, sorts them by name, and runs each one
through `Main`. It captures standard output and standard error separately,
in memory (it does not create output files), then compares each with the
matching expected files. It prints `PASS` for a matching case and `FAIL` with
the differing output otherwise. The final line shows how many cases passed.
The runner exits with a nonzero status if any case fails.

To run tests from a different fixture directory, pass that directory as the
test runner's argument:

```text
java -cp src:tests TestRunner path/to/cases
```

There are 16 test cases covering all tokens, identifiers, numbers, strings,
symbols, whitespace, comments, token precedence, source positions, invalid
characters, empty input, and UTF-8/Unicode columns. The individual cases and
their recorded results are listed in
[`../tests/TEST_RESULTS.md`](../tests/TEST_RESULTS.md).

## Bug fixes found during testing

Testing exposed two problems in the scanner implementation:

1. The generated `Lexer.yylex()` method returns `void`, but its end-of-file
   branch used `return null;`. This prevented the Java sources from compiling.
   The branch now uses `return;`.
2. JFlex counts UTF-16 code units in its column counter. A supplementary
   Unicode character uses two code units, even though it is one character for
   the displayed column. The lexer now corrects the column after processing
   tokens, ignored text, and unexpected characters.

The Unicode correction is present in both `scanner.flex` and the generated
`Lexer.java`. If the scanner is regenerated from the specification file, keep
the generated scanner in sync with those rules.