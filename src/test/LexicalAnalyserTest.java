package test;

import analyser.LexicalAnalyser;
import models.Decimal;
import models.Num;
import models.Tag;
import models.Token;
import models.Word;
import utils.exceptions.LexicalException;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Suite de testes para o LexicalAnalyser.
 * Autocontida — sem dependências externas. Execute via: java test.LexicalAnalyserTest
 */
public class LexicalAnalyserTest {

    // -------------------------------------------------------------------------
    // Mini-framework
    // -------------------------------------------------------------------------

    private static int passed = 0;
    private static int failed = 0;

    private static void test(String name, Runnable body) {
        try {
            body.run();
            System.out.printf("  PASS  %s%n", name);
            passed++;
        } catch (Throwable t) {
            System.out.printf("  FAIL  %s  —  %s%n", name, t.getMessage());
            failed++;
        }
    }

    private static void assertEquals(Object expected, Object actual, String msg) {
        if (!expected.equals(actual))
            throw new AssertionError(msg + " | esperado=" + expected + " obtido=" + actual);
    }

    private static void assertTrue(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
    }

    @FunctionalInterface
    interface ThrowingRunnable { void run() throws Exception; }

    private static void assertThrows(Class<? extends Throwable> type,
                                     ThrowingRunnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + " | esperava " + type.getSimpleName() + " mas nada foi lançado");
        } catch (Throwable t) {
            if (!type.isInstance(t))
                throw new AssertionError(
                    msg + " | esperava " + type.getSimpleName()
                    + " mas obteve " + t.getClass().getSimpleName() + ": " + t.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static LexicalAnalyser lexer(String input) {
        return new LexicalAnalyser(new StringReader(input));
    }

    /** Lê todos os tokens até EOF (inclusive). */
    private static List<Token> scanAll(String input) {
        LexicalAnalyser la = lexer(input);
        List<Token> tokens = new ArrayList<>();
        Token t;
        do {
            t = la.scan();
            tokens.add(t);
        } while (t.getTag() != Tag.EOF);
        return tokens;
    }

    /** Lê exatamente n tokens. */
    private static List<Token> scanN(String input, int n) {
        LexicalAnalyser la = lexer(input);
        List<Token> tokens = new ArrayList<>();
        for (int i = 0; i < n; i++) tokens.add(la.scan());
        return tokens;
    }

    // -------------------------------------------------------------------------
    // Grupo 1 — EOF
    // -------------------------------------------------------------------------

    static void testEof() {
        test("EOF em entrada vazia", () -> {
            List<Token> tokens = scanAll("");
            assertEquals(1, tokens.size(), "tamanho");
            assertEquals(Tag.EOF, tokens.get(0).getTag(), "tag");
        });

        test("EOF após espaços", () -> {
            List<Token> tokens = scanAll("   ");
            assertEquals(1, tokens.size(), "tamanho");
            assertEquals(Tag.EOF, tokens.get(0).getTag(), "tag");
        });
    }

    // -------------------------------------------------------------------------
    // Grupo 2 — Espaços em branco e contagem de linhas
    // -------------------------------------------------------------------------

    static void testWhitespace() {
        test("Espaço é ignorado", () -> {
            List<Token> tokens = scanAll(" + ");
            assertEquals(Tag.PLUS, tokens.get(0).getTag(), "tag +");
        });

        test("Tab é ignorado", () -> {
            List<Token> tokens = scanAll("\t+");
            assertEquals(Tag.PLUS, tokens.get(0).getTag(), "tag +");
        });

        test("Carriage-return é ignorado", () -> {
            List<Token> tokens = scanAll("\r+");
            assertEquals(Tag.PLUS, tokens.get(0).getTag(), "tag +");
        });

        test("Newline incrementa linha", () -> {
            LexicalAnalyser la = lexer("a\nb");
            la.scan(); // 'a' — linha 1
            assertEquals(1, la.getCurrentLine(), "linha após 'a'");
            la.scan(); // 'b' — newline incrementa antes de ler 'b'
            assertEquals(2, la.getCurrentLine(), "linha após newline");
        });

        test("Múltiplos newlines acumulam contagem", () -> {
            LexicalAnalyser la = lexer("\n\n\na");
            la.scan(); // 'a'
            assertEquals(4, la.getCurrentLine(), "linha após 3 newlines");
        });
    }

    // -------------------------------------------------------------------------
    // Grupo 3 — Operadores de um caractere
    // -------------------------------------------------------------------------

    static void testSingleCharOperators() {
        int[][] cases = {
            {'+', Tag.PLUS},
            {'-', Tag.MINUS},
            {'*', Tag.TIMES},
            {'%', Tag.MOD},
            {'=', Tag.EQUAL},
            {'>', Tag.GT},
            {'(', Tag.LPAREN},
            {')', Tag.RPAREN},
            {'{', Tag.LBRACE},
            {'}', Tag.RBRACE},
            {';', Tag.SEMICOLON},
            {',', Tag.COMMA},
            {':', Tag.COLON},
        };
        for (int[] pair : cases) {
            char ch = (char) pair[0];
            int expected = pair[1];
            test("Operador '" + ch + "'", () ->
                assertEquals(expected, scanAll(String.valueOf(ch)).get(0).getTag(), "tag"));
        }

        test("'&' isolado retorna tag '&'", () ->
            assertEquals((int)'&', scanAll("&").get(0).getTag(), "tag"));

        test("'|' isolado retorna tag '|'", () ->
            assertEquals((int)'|', scanAll("|").get(0).getTag(), "tag"));

        test("'!' isolado retorna tag '!'", () ->
            assertEquals((int)'!', scanAll("!").get(0).getTag(), "tag"));
    }

    // -------------------------------------------------------------------------
    // Grupo 4 — Operadores compostos (múltiplos caracteres)
    // -------------------------------------------------------------------------

    static void testCompoundOperators() {
        test(":= retorna ASSIGN", () -> {
            Token t = scanAll(":=").get(0);
            assertEquals(Tag.ASSIGN, t.getTag(), "tag");
            assertEquals(":=", ((Word) t).getLexeme(), "lexema");
        });

        test(">= retorna GE", () -> {
            Token t = scanAll(">=").get(0);
            assertEquals(Tag.GE, t.getTag(), "tag");
            assertEquals(">=", ((Word) t).getLexeme(), "lexema");
        });

        test("<= retorna LE", () -> {
            Token t = scanAll("<=").get(0);
            assertEquals(Tag.LE, t.getTag(), "tag");
            assertEquals("<=", ((Word) t).getLexeme(), "lexema");
        });

        test("<> retorna NE", () -> {
            Token t = scanAll("<>").get(0);
            assertEquals(Tag.NE, t.getTag(), "tag");
            assertEquals("<>", ((Word) t).getLexeme(), "lexema");
        });

        test("&& retorna AND", () -> {
            Token t = scanAll("&&").get(0);
            assertEquals(Tag.AND, t.getTag(), "tag");
            assertEquals("&&", ((Word) t).getLexeme(), "lexema");
        });

        test("|| retorna OR", () -> {
            Token t = scanAll("||").get(0);
            assertEquals(Tag.OR, t.getTag(), "tag");
            assertEquals("||", ((Word) t).getLexeme(), "lexema");
        });

        test("> sozinho retorna GT", () ->
            assertEquals(Tag.GT, scanAll(">").get(0).getTag(), "tag"));

        test("< sozinho retorna LT", () ->
            assertEquals(Tag.LT, scanAll("<").get(0).getTag(), "tag"));

        test(": sozinho retorna COLON", () ->
            assertEquals(Tag.COLON, scanAll(":").get(0).getTag(), "tag"));

        test("< seguido de letra retorna LT e processa letra", () -> {
            List<Token> tokens = scanAll("<a");
            assertEquals(Tag.LT, tokens.get(0).getTag(), "tag LT");
            assertEquals(Tag.ID, tokens.get(1).getTag(), "tag ID");
        });
    }

    // -------------------------------------------------------------------------
    // Grupo 5 — Barra: divisão e comentários
    // -------------------------------------------------------------------------

    static void testSlashAndComments() {
        test("'/' sozinho retorna DIVIDE", () ->
            assertEquals(Tag.DIVIDE, scanAll("/").get(0).getTag(), "tag"));

        test("'/' seguido de outro char retorna DIVIDE e processa próximo", () -> {
            List<Token> tokens = scanAll("/+");
            assertEquals(Tag.DIVIDE, tokens.get(0).getTag(), "tag /");
            assertEquals(Tag.PLUS, tokens.get(1).getTag(), "tag +");
        });

        test("Comentário de linha é ignorado", () -> {
            List<Token> tokens = scanAll("// comentário\n+");
            assertEquals(Tag.PLUS, tokens.get(0).getTag(), "tag após comentário");
        });

        test("Comentário de linha no fim do arquivo é ignorado", () -> {
            List<Token> tokens = scanAll("// sem newline ao final");
            assertEquals(Tag.EOF, tokens.get(0).getTag(), "tag EOF");
        });

        test("Comentário de bloco simples é ignorado", () -> {
            List<Token> tokens = scanAll("/* bloco */+");
            assertEquals(Tag.PLUS, tokens.get(0).getTag(), "tag após bloco");
        });

        test("Comentário de bloco multilinha incrementa linhas", () -> {
            LexicalAnalyser la = lexer("/* linha1\nlinha2\n*/+");
            la.scan(); // '+'
            assertEquals(3, la.getCurrentLine(), "linha após bloco multilinhas");
        });

        test("Comentário de bloco com * interno não fecha cedo", () -> {
            List<Token> tokens = scanAll("/* a * b */+");
            assertEquals(Tag.PLUS, tokens.get(0).getTag(), "tag após bloco com *");
        });

        test("Comentário de bloco não-fechado lança LexicalException", () ->
            assertThrows(LexicalException.class,
                () -> scanAll("/* sem fechamento"),
                "comentário não-fechado"));

        test("'/**/' é reconhecido como bloco fechado", () -> {
            List<Token> tokens = scanAll("/**/+");
            assertEquals(Tag.PLUS, tokens.get(0).getTag(), "tag após /**/");
        });
    }

    // -------------------------------------------------------------------------
    // Grupo 6 — Literais string
    // -------------------------------------------------------------------------

    static void testStringLiterals() {
        test("String simples retorna Word com tag STRING", () -> {
            Token t = scanAll("\"hello\"").get(0);
            assertEquals(Tag.STRING, t.getTag(), "tag");
            assertEquals("hello", ((Word) t).getLexeme(), "lexema");
        });

        test("String vazia retorna Word com lexema vazio", () -> {
            Token t = scanAll("\"\"").get(0);
            assertEquals(Tag.STRING, t.getTag(), "tag");
            assertEquals("", ((Word) t).getLexeme(), "lexema");
        });

        test("String com espaços preserva conteúdo", () -> {
            Token t = scanAll("\"ola mundo\"").get(0);
            assertEquals("ola mundo", ((Word) t).getLexeme(), "lexema");
        });

        test("String não-fechada (EOF) lança LexicalException", () ->
            assertThrows(LexicalException.class,
                () -> scanAll("\"sem fechamento"),
                "string não-fechada"));
    }

    // -------------------------------------------------------------------------
    // Grupo 7 — Literais numéricos
    // -------------------------------------------------------------------------

    static void testNumericLiterals() {
        test("Zero retorna Num(0)", () -> {
            Token t = scanAll("0").get(0);
            assertEquals(Tag.NUM, t.getTag(), "tag");
            assertEquals(0, ((Num) t).getValue(), "valor");
        });

        test("Inteiro simples retorna Num correto", () -> {
            Token t = scanAll("42").get(0);
            assertEquals(Tag.NUM, t.getTag(), "tag");
            assertEquals(42, ((Num) t).getValue(), "valor");
        });

        test("Inteiro com múltiplos dígitos", () -> {
            Token t = scanAll("1234").get(0);
            assertEquals(1234, ((Num) t).getValue(), "valor");
        });

        test("Número real retorna Decimal com tag REAL", () -> {
            Token t = scanAll("3.14").get(0);
            assertEquals(Tag.REAL, t.getTag(), "tag");
            assertTrue(t instanceof Decimal, "instância de Decimal");
            double v = ((Decimal) t).getValue();
            assertTrue(Math.abs(v - 3.14) < 0.0001, "valor ≈ 3.14");
        });

        test("Número real iniciando com zero: 0.5", () -> {
            Token t = scanAll("0.5").get(0);
            assertEquals(Tag.REAL, t.getTag(), "tag");
            assertTrue(Math.abs(((Decimal) t).getValue() - 0.5) < 0.0001, "valor ≈ 0.5");
        });

        test("Ponto sem dígito após lança LexicalException", () ->
            assertThrows(LexicalException.class,
                () -> scanAll("3.x"),
                "decimal malformado"));

        test("Inteiro seguido de ponto-e-vírgula: dois tokens", () -> {
            List<Token> tokens = scanAll("10;");
            assertEquals(Tag.NUM, tokens.get(0).getTag(), "tag NUM");
            assertEquals(Tag.SEMICOLON, tokens.get(1).getTag(), "tag ;");
        });
    }

    // -------------------------------------------------------------------------
    // Grupo 8 — Identificadores e palavras reservadas
    // -------------------------------------------------------------------------

    static void testIdentifiersAndKeywords() {
        test("Identificador simples retorna ID", () -> {
            Token t = scanAll("abc").get(0);
            assertEquals(Tag.ID, t.getTag(), "tag");
            assertEquals("abc", ((Word) t).getLexeme(), "lexema");
        });

        test("Identificador com dígitos no meio", () -> {
            Token t = scanAll("a1b2").get(0);
            assertEquals(Tag.ID, t.getTag(), "tag");
            assertEquals("a1b2", ((Word) t).getLexeme(), "lexema");
        });

        test("Mesmo identificador retorna a mesma instância (tabela de símbolos)", () -> {
            LexicalAnalyser la = lexer("x x");
            Token t1 = la.scan();
            Token t2 = la.scan();
            assertTrue(t1 == t2, "mesma referência da tabela de símbolos");
        });

        // Palavras reservadas
        String[][] keywords = {
            {"class",  String.valueOf(Tag.CLASS)},
            {"int",    String.valueOf(Tag.INT)},
            {"string", String.valueOf(Tag.STRING)},
            {"float",  String.valueOf(Tag.FLOAT)},
            {"if",     String.valueOf(Tag.IF)},
            {"else",   String.valueOf(Tag.ELSE)},
            {"do",     String.valueOf(Tag.DO)},
            {"while",  String.valueOf(Tag.WHILE)},
            {"repeat", String.valueOf(Tag.REPEAT)},
            {"until",  String.valueOf(Tag.UNTIL)},
            {"read",   String.valueOf(Tag.READ)},
            {"write",  String.valueOf(Tag.WRITE)},
            {"not",    String.valueOf(Tag.NOT)},
            {"or",     String.valueOf(Tag.OR)},
            {"and",    String.valueOf(Tag.AND)},
        };
        for (String[] kw : keywords) {
            String word = kw[0];
            int expectedTag = Integer.parseInt(kw[1]);
            test("Palavra reservada '" + word + "'", () -> {
                Token t = scanAll(word).get(0);
                assertEquals(expectedTag, t.getTag(), "tag");
                assertEquals(word, ((Word) t).getLexeme(), "lexema");
            });
        }

        test("Linguagem é case-sensitive: 'IF' é identificador, não palavra reservada", () -> {
            Token t = scanAll("IF").get(0);
            assertEquals(Tag.ID, t.getTag(), "tag — IF maiúsculo deve ser ID");
        });
    }

    // -------------------------------------------------------------------------
    // Grupo 9 — Caracteres desconhecidos
    // -------------------------------------------------------------------------

    static void testUnknownChars() {
        test("Caractere desconhecido '@' retorna Token com tag = (int)'@'", () -> {
            Token t = scanAll("@").get(0);
            assertEquals((int)'@', t.getTag(), "tag");
        });

        test("Caractere desconhecido '#' retorna Token com tag = (int)'#'", () -> {
            Token t = scanAll("#").get(0);
            assertEquals((int)'#', t.getTag(), "tag");
        });
    }

    // -------------------------------------------------------------------------
    // Grupo 10 — Construtor por arquivo
    // -------------------------------------------------------------------------

    static void testFileConstructor() {
        test("Arquivo inexistente lança LexicalException", () ->
            assertThrows(LexicalException.class,
                () -> new LexicalAnalyser("nao_existe.txt"),
                "arquivo inexistente"));
    }

    // -------------------------------------------------------------------------
    // Grupo 11 — Sequências mistas (integração)
    // -------------------------------------------------------------------------

    static void testMixedSequences() {
        test("Declaração simples: 'int x;'", () -> {
            List<Token> tokens = scanAll("int x;");
            assertEquals(Tag.INT,       tokens.get(0).getTag(), "int");
            assertEquals(Tag.ID,        tokens.get(1).getTag(), "x");
            assertEquals(Tag.SEMICOLON, tokens.get(2).getTag(), ";");
            assertEquals(Tag.EOF,       tokens.get(3).getTag(), "EOF");
        });

        test("Atribuição: 'x := 10'", () -> {
            List<Token> tokens = scanAll("x := 10");
            assertEquals(Tag.ID,     tokens.get(0).getTag(), "x");
            assertEquals(Tag.ASSIGN, tokens.get(1).getTag(), ":=");
            assertEquals(Tag.NUM,    tokens.get(2).getTag(), "10");
        });

        test("Condição com operador relacional '<>'", () -> {
            List<Token> tokens = scanAll("a <> b");
            assertEquals(Tag.ID, tokens.get(0).getTag(), "a");
            assertEquals(Tag.NE, tokens.get(1).getTag(), "<>");
            assertEquals(Tag.ID, tokens.get(2).getTag(), "b");
        });

        test("Expressão: 'a + b * 2'", () -> {
            List<Token> tokens = scanAll("a + b * 2");
            assertEquals(Tag.ID,    tokens.get(0).getTag(), "a");
            assertEquals(Tag.PLUS,  tokens.get(1).getTag(), "+");
            assertEquals(Tag.ID,    tokens.get(2).getTag(), "b");
            assertEquals(Tag.TIMES, tokens.get(3).getTag(), "*");
            assertEquals(Tag.NUM,   tokens.get(4).getTag(), "2");
        });

        test("Comentário inline não interfere com tokens adjacentes", () -> {
            List<Token> tokens = scanAll("a // ignorado\nb");
            assertEquals(Tag.ID, tokens.get(0).getTag(), "a");
            assertEquals(Tag.ID, tokens.get(1).getTag(), "b");
            assertEquals(Tag.EOF, tokens.get(2).getTag(), "EOF");
        });

        test("Bloco de código mínimo válido", () -> {
            String src = "class Foo { }";
            List<Token> tokens = scanAll(src);
            assertEquals(Tag.CLASS,  tokens.get(0).getTag(), "class");
            assertEquals(Tag.ID,     tokens.get(1).getTag(), "Foo");
            assertEquals(Tag.LBRACE, tokens.get(2).getTag(), "{");
            assertEquals(Tag.RBRACE, tokens.get(3).getTag(), "}");
            assertEquals(Tag.EOF,    tokens.get(4).getTag(), "EOF");
        });

        test("write com literal string", () -> {
            List<Token> tokens = scanAll("write(\"ola\")");
            assertEquals(Tag.WRITE,   tokens.get(0).getTag(), "write");
            assertEquals(Tag.LPAREN,  tokens.get(1).getTag(), "(");
            assertEquals(Tag.STRING,  tokens.get(2).getTag(), "\"ola\"");
            assertEquals(Tag.RPAREN,  tokens.get(3).getTag(), ")");
        });

        test("read com identificador", () -> {
            List<Token> tokens = scanAll("read(x)");
            assertEquals(Tag.READ,   tokens.get(0).getTag(), "read");
            assertEquals(Tag.LPAREN, tokens.get(1).getTag(), "(");
            assertEquals(Tag.ID,     tokens.get(2).getTag(), "x");
            assertEquals(Tag.RPAREN, tokens.get(3).getTag(), ")");
        });

        test("Operadores relacionais: > >= < <= <> =", () -> {
            List<Token> tokens = scanAll("> >= < <= <> =");
            assertEquals(Tag.GT,    tokens.get(0).getTag(), ">");
            assertEquals(Tag.GE,    tokens.get(1).getTag(), ">=");
            assertEquals(Tag.LT,    tokens.get(2).getTag(), "<");
            assertEquals(Tag.LE,    tokens.get(3).getTag(), "<=");
            assertEquals(Tag.NE,    tokens.get(4).getTag(), "<>");
            assertEquals(Tag.EQUAL, tokens.get(5).getTag(), "=");
        });

        test("Operadores aritméticos: + - * / %", () -> {
            List<Token> tokens = scanAll("+ - * / %");
            assertEquals(Tag.PLUS,   tokens.get(0).getTag(), "+");
            assertEquals(Tag.MINUS,  tokens.get(1).getTag(), "-");
            assertEquals(Tag.TIMES,  tokens.get(2).getTag(), "*");
            assertEquals(Tag.DIVIDE, tokens.get(3).getTag(), "/");
            assertEquals(Tag.MOD,    tokens.get(4).getTag(), "%");
        });
    }

    // -------------------------------------------------------------------------
    // Grupo 12 — toString dos modelos
    // -------------------------------------------------------------------------

    static void testToString() {
        test("Token.toString()", () ->
            assertTrue(new Token(Tag.EOF).toString().contains(String.valueOf(Tag.EOF)), "contém tag"));

        test("Num.toString()", () ->
            assertTrue(new Num(7).toString().contains("7"), "contém valor"));

        test("Decimal.toString()", () ->
            assertTrue(new Decimal(1.5).toString().contains("1.5"), "contém valor"));

        test("Word.toString()", () ->
            assertTrue(new Word("if", Tag.IF).toString().contains("if"), "contém lexema"));
    }

    // -------------------------------------------------------------------------
    // main
    // -------------------------------------------------------------------------

    public static void main(String[] args) {
        System.out.println("=== LexicalAnalyser — Testes ===\n");

        System.out.println("[ EOF ]");
        testEof();

        System.out.println("\n[ Espaços em branco e linhas ]");
        testWhitespace();

        System.out.println("\n[ Operadores de um caractere ]");
        testSingleCharOperators();

        System.out.println("\n[ Operadores compostos ]");
        testCompoundOperators();

        System.out.println("\n[ Barra e comentários ]");
        testSlashAndComments();

        System.out.println("\n[ Literais string ]");
        testStringLiterals();

        System.out.println("\n[ Literais numéricos ]");
        testNumericLiterals();

        System.out.println("\n[ Identificadores e palavras reservadas ]");
        testIdentifiersAndKeywords();

        System.out.println("\n[ Caracteres desconhecidos ]");
        testUnknownChars();

        System.out.println("\n[ Construtor por arquivo ]");
        testFileConstructor();

        System.out.println("\n[ Sequências mistas ]");
        testMixedSequences();

        System.out.println("\n[ toString dos modelos ]");
        testToString();

        System.out.printf("%n=== Resultado: %d aprovados, %d reprovados ===%n", passed, failed);
        if (failed > 0) System.exit(1);
    }
}
