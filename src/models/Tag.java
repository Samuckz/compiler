package models;

public class Tag {
    private int tag;

    public final static int
            // Palavras reservadas
            CLASS = 256,
            INT = 257,
            STRING = 258,
            FLOAT = 259,
            IF = 260,
            ELSE = 261,
            DO = 262,
            WHILE = 263,
            REPEAT = 264,
            UNTIL = 265,
            READ = 266,
            WRITE = 267,
            NOT = 268,
            OR = 269,
            AND = 270,

            // Operadores compostos (múltiplos caracteres)
            ASSIGN = 280,   // :=
            GE = 281,       // >=
            LE = 282,       // <=
            NE = 283,       // <>

            // Literais e identificadores
            NUM = 284,
            ID = 285,
            TRUE = 286,
            FALSE = 287,
            EOF = 288,
            REAL = 289,     // constante real (ex: 3.14)

            // Operadores de um caractere (valor = código ASCII)
            PLUS     = '+',  // 43
            MINUS    = '-',  // 45
            TIMES    = '*',  // 42
            DIVIDE   = '/',  // 47
            MOD      = '%',  // 37
            EQUAL    = '=',  // 61  (operador relacional de igualdade)
            LT       = '<',  // 60
            GT       = '>',  // 62

            // Pontuação de um caractere
            LPAREN    = '(',  // 40
            RPAREN    = ')',  // 41
            LBRACE    = '{',  // 123
            RBRACE    = '}',  // 125
            SEMICOLON = ';',  // 59
            COMMA     = ',',  // 44
            COLON     = ':';  // 58
}
