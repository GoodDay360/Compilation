import java.io.*;

%%

%class Lexer
%public
%unicode
%line
%column
%type void

%{
    private void printToken(String type, String text) {
        System.out.printf("Line %d, Column %d: %s \"%s\"\n", yyline + 1, yycolumn + 1, type, text);
    }

    private void emitToken(String type, String text) {
        printToken(type, text);
        adjustUnicodeColumn(text);
    }

    // JFlex counts UTF-16 code units for columns. A supplementary character such as an emoji
    // uses two code units but is one Unicode character, so subtract the extra unit from the column.
    private void adjustUnicodeColumn(String text) {
        for (int i = 0; i < text.length();) {
            int codePoint = text.codePointAt(i);
            if (Character.charCount(codePoint) == 2) {
                yycolumn--;
            }
            i += Character.charCount(codePoint);
        }
    }
%}

Identifier = [a-zA-Z_][a-zA-Z0-9_]*
Integer    = [0-9]+
Decimal    = [0-9]+"."[0-9]+
String     = \"[^\r\n\"]*\"
Whitespace = [ \t\r\n]+
Comment    = "#"[^\r\n]*

%%

<YYINITIAL> {
    "["              { emitToken("LBRACKET", yytext()); }
    "]"              { emitToken("RBRACKET", yytext()); }
    "="              { emitToken("ASSIGN", yytext()); }
    "true" | "false" { emitToken("BOOLEAN", yytext()); }
    {Identifier}     { emitToken("IDENTIFIER", yytext()); }
    {Decimal}        { emitToken("DECIMAL", yytext()); }
    {Integer}        { emitToken("INTEGER", yytext()); }
    {String}         { emitToken("STRING", yytext()); }
    {Whitespace}     { adjustUnicodeColumn(yytext()); }
    {Comment}        { adjustUnicodeColumn(yytext()); }
    [^]              {
                         System.err.printf("Error: Unexpected character '%s' at Line %d, Column %d\n", yytext(), yyline + 1, yycolumn + 1);
                         adjustUnicodeColumn(yytext());
                     }
}
