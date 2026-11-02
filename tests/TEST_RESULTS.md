# Test results

Compile and run from the project root:

```text
mkdir -p out
javac -encoding UTF-8 -d out src/Lexer.java src/Main.java tests/TestRunner.java
java -cp out TestRunner
```

The `-d out` option puts compiled classes in the `out` directory. The Java
test runner compares standard output and standard error against the expected
files in `tests/cases/`.

| # | Case | Coverage | Result |
|---|---|---|---|
| 1 | `01_all_tokens` | All token types in a configuration | PASS |
| 2 | `02_identifiers` | Identifier start and continuation characters | PASS |
| 3 | `03_integers` | Integer token boundaries | PASS |
| 4 | `04_decimals` | Decimal tokens | PASS |
| 5 | `05_booleans` | Both boolean literals | PASS |
| 6 | `06_strings` | Regular, empty, and UTF-8 strings | PASS |
| 7 | `07_symbols` | Brackets and assignment | PASS |
| 8 | `08_whitespace` | Spaces and tabs | PASS |
| 9 | `09_comments` | Full-line and inline comments | PASS |
| 10 | `10_positions` | Line/column tracking | PASS |
| 11 | `11_errors` | Error reporting and continued scanning | PASS |
| 12 | `12_longest_match` | Boolean/identifier and decimal/integer precedence | PASS |
| 13 | `13_unicode_columns` | UTF-8 and supplementary Unicode column counting | PASS |
| 14 | `14_invalid_decimal` | Invalid punctuation and continued scanning | PASS |
| 15 | `15_empty` | Input containing no tokens | PASS |
| 16 | `16_unicode_error` | Unicode error reporting and following token position | PASS |
