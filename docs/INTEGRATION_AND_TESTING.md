# Java integration and testing

The lexer rules are in `src/scanner.flex`. `src/Lexer.java` is the generated
scanner, and `src/Main.java` runs it on a configuration file.

The program takes one file path. It prints tokens to standard output and
unexpected-character errors to standard error. It ignores spaces and `#`
comments. Token positions start at line 1, column 1. Unicode characters such
as emoji count as one column.

The lexer uses JFlex's longest-match rule: `true` is a `BOOLEAN`, `trueValue`
is an `IDENTIFIER`, and `30.5` is a `DECIMAL`.

## Run the scanner

From the project root, change to `src`:

```text
cd src
```

With Java 11 or newer, run the source directly:

```text
java Main.java ../tests/cases/01_all_tokens.cfg
```

Or compile the classes first:

```text
javac -encoding UTF-8 Lexer.java Main.java
java Main ../tests/cases/01_all_tokens.cfg
```

Replace the sample path with the configuration file you want to scan. Relative
paths are resolved from the current directory (`src` in these examples).

Tokens appear in the terminal by default. To save them to a file, redirect
standard output with `>`:

```text
java Main.java ../tests/cases/01_all_tokens.cfg > output.txt
```

Errors still appear in the terminal. To save them separately, use `2>`:

```text
java Main.java ../tests/cases/11_errors.cfg > output.txt 2> errors.txt
```

These commands create the output files in the current directory. The `.out`
and `.err` files in `tests/cases/` are expected results; running `Main` does
not change them.

## Run the tests

Each `.cfg` input in `tests/cases/` has a matching `.out` file. Cases that
expect errors also have a `.err` file.

From the project root, run all tests with Java 11 or newer:

```text
java src/TestRunner.java
```

You can also run `java TestRunner.java` from `src`. The runner checks each case
against its expected output, prints `PASS` or `FAIL`, and exits with an error
status if any case fails.

To compile and run the test runner instead:

```text
mkdir -p out
javac -encoding UTF-8 -d out src/Lexer.java src/Main.java src/TestRunner.java
java -cp out TestRunner
```

To use a different folder of test cases with the compiled runner, pass its
path as an argument:

```text
java -cp out TestRunner path/to/cases
```

The 16 cases cover tokens, identifiers, numbers, strings, symbols, whitespace,
comments, token precedence, positions, errors, and Unicode. See
[`../tests/TEST_RESULTS.md`](../tests/TEST_RESULTS.md) for the results.
