package models;

public class Tag {
    private Integer tag;

    public final static int
            //Palavras reservadas
            // Palavras reservadas da linguagem
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

            // Operadores e pontuação


            //Operadores e pontuação
            EQ = 280,
            GE = 281,
            LE = 282,
            NE = 283,

            //Outros tokens
            NUM = 284,
            ID = 285,
            TRUE = 286,
            FALSE  = 287,
            EOF = 288;
}
