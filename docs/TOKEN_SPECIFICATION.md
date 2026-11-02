# Token Specification
**Project:** Configuration File Scanner
**Course:** Compilation
**Tool:** JFlex and java

## 1. Token Definitions

| Token Type | Regular Expression | Example | Description |
|---|---|---|---|
| IDENTIFIER | `[a-zA-Z_][a-zA-Z0-9_]*` | `host` | Names can start with letter or underscore |
| INTEGER | `[0-9]+` | `3306` | Numbers like 1 to 9 and so on |
| DECIMAL | `[0-9]+"."[0-9]+` | `30.5` | numbers contains like decimal |
| BOOLEAN | `"true"|"false"` | `true` | Boolean values |
| STRING | `\"[^\r\n\"]*\"` | `"localhost"` | Text enclosed in double quotes |
| ASSIGN | `"="` | `=` | Assignment operator |
| LBRACKET | `"["` | `[` | Opening square bracket |
| RBRACKET | `"]"` | `]` | Closing square bracket |

## 2. Ignored Input

| Category | Regular Expression | Example | Action |
|---|---|---|---|
| WHITESPACE | `[ \t\r\n]+` | \n = new line, \t = tab or like space \r = return | Ignore |
| COMMENT | `"#"[^\r\n]*` | `# Database configuration` | Ignore |

## 3. Competing Rules

### Case 1: BOOLEAN and IDENTIFIER

- Input: `true` or `false`

Both token patterns can match the same length. The boolean win if they match the pattern like length or character. So boolean will be the priority.

- Input: `trueValue`

IDENTIFIER wins because it matches the longer string.

### Case 2: DECIMAL and INTEGER

- Input: `30.5`

INTEGER matches `30`, while DECIMAL matches `30.5`. JFlex chooses DECIMAL because it matches more characters.



## 4. Error Handling

Unexpected characters that do not match any valid token will be reported with their line and column positions. The scanner will continue scanning after reporting the error.