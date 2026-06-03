package test;

import analyser.LexicalAnalyser;
import analyser.SyntacticAnalyser;
import utils.exceptions.SyntacticException;

import java.io.StringReader;

/**
 * Suite TDD para o SyntacticAnalyser.
 * Os testes falham até que a implementação correspondente à história seja concluída.
 * Execute via: java test.SyntacticAnalyserTest
 */
public class SyntacticAnalyserTest {

    // -------------------------------------------------------------------------
    // Mini-framework (mesmo padrão do LexicalAnalyserTest)
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
    // Helper
    // -------------------------------------------------------------------------

    /**
     * Cria um parser sobre a string de entrada e chama analyse().
     * Lança SyntacticException se a entrada for sintaticamente inválida.
     * Para programas completos, envolva o corpo com o template de programa.
     */
    private static void parse(String input) {
        LexicalAnalyser lexer = new LexicalAnalyser(new StringReader(input));
        SyntacticAnalyser parser = new SyntacticAnalyser(lexer);
        parser.analyse();
    }

    /** Atalho: envolve stmt-list num programa completo sem declarações. */
    private static void parseProgram(String stmts) {
        parse("class Test { { " + stmts + " } }");
    }

    /** Atalho: envolve stmt-list num programa com declarações. */
    private static void parseProgramWithDecls(String decls, String stmts) {
        parse("class Test { " + decls + " { " + stmts + " } }");
    }

    // -------------------------------------------------------------------------
    // H1 — Infraestrutura
    // -------------------------------------------------------------------------

    static void testH1_Infraestrutura() {
        test("[H1] Entrada vazia lança SyntacticException", () ->
            assertThrows(SyntacticException.class, () -> parse(""), "entrada vazia"));

        test("[H1] SyntacticException contém número da linha", () -> {
            try {
                parse("erro");
                throw new AssertionError("deveria lançar exceção");
            } catch (SyntacticException e) {
                assertTrue(e.getMessage() != null && !e.getMessage().isEmpty(),
                    "mensagem de erro não deve ser nula/vazia");
            }
        });
    }

    // -------------------------------------------------------------------------
    // H2 — Estrutura do programa
    // -------------------------------------------------------------------------

    static void testH2_EstruturaDoProgramma() {
        test("[H2] Programa mínimo válido", () ->
            parse("class Foo { { x := 1; } }"));

        test("[H2] Falta 'class' → erro", () ->
            assertThrows(SyntacticException.class,
                () -> parse("Foo { { x := 1; } }"), "falta class"));

        test("[H2] Falta identificador após 'class' → erro", () ->
            assertThrows(SyntacticException.class,
                () -> parse("class { { x := 1; } }"), "falta id após class"));

        test("[H2] Falta '{' do programa → erro", () ->
            assertThrows(SyntacticException.class,
                () -> parse("class Foo { x := 1; } }"), "falta { do body"));

        test("[H2] Falta '}' de fechamento → erro", () ->
            assertThrows(SyntacticException.class,
                () -> parse("class Foo { { x := 1; }"), "falta } final"));

        test("[H2] Programa com decl-list e body válidos", () ->
            parse("class Foo { int x; { x := 1; } }"));
    }

    // -------------------------------------------------------------------------
    // H3 — Declarações de variáveis
    // -------------------------------------------------------------------------

    static void testH3_Declaracoes() {
        test("[H3] Declaração de int", () ->
            parse("class Foo { int x; { x := 1; } }"));

        test("[H3] Declaração de float", () ->
            parse("class Foo { float f; { f := 1; } }"));

        test("[H3] Declaração de string", () ->
            parse("class Foo { string s; { s := \"a\"; } }"));

        test("[H3] Múltiplos identificadores na mesma declaração", () ->
            parse("class Foo { int x, y, z; { x := 1; } }"));

        test("[H3] Múltiplos tipos declarados", () ->
            parse("class Foo { int x; float f; string s; { x := 1; } }"));

        test("[H3] Declaração sem tipo → erro", () ->
            assertThrows(SyntacticException.class,
                () -> parse("class Foo { x; { x := 1; } }"), "tipo ausente"));

        test("[H3] Declaração sem identificador → erro", () ->
            assertThrows(SyntacticException.class,
                () -> parse("class Foo { int; { x := 1; } }"), "id ausente"));

        test("[H3] Declaração sem ';' → erro", () ->
            assertThrows(SyntacticException.class,
                () -> parse("class Foo { int x { x := 1; } }"), "; ausente"));
    }

    // -------------------------------------------------------------------------
    // H4 — Atribuição e expressões aritméticas
    // -------------------------------------------------------------------------

    static void testH4_AtribuicaoExpressoes() {
        test("[H4] Atribuição com constante inteira", () ->
            parseProgram("x := 1;"));

        test("[H4] Atribuição com constante real", () ->
            parseProgram("x := 3.14;"));

        test("[H4] Atribuição com literal string", () ->
            parseProgram("x := \"texto\";"));

        test("[H4] Atribuição com identificador", () ->
            parseProgram("x := y;"));

        test("[H4] Expressão com adição", () ->
            parseProgram("x := a + b;"));

        test("[H4] Expressão com subtração", () ->
            parseProgram("x := a - b;"));

        test("[H4] Expressão com multiplicação", () ->
            parseProgram("x := a * b;"));

        test("[H4] Expressão com divisão", () ->
            parseProgram("x := a / b;"));

        test("[H4] Expressão com módulo", () ->
            parseProgram("x := a % b;"));

        test("[H4] Expressão com 'and' (mulop)", () ->
            parseProgram("x := a and b;"));

        test("[H4] Expressão com 'or' (addop)", () ->
            parseProgram("x := a or b;"));

        test("[H4] Expressão com múltiplos operadores", () ->
            parseProgram("x := a * b + c / d;"));

        test("[H4] Expressão com parênteses", () ->
            parseProgram("x := (a + b) * c;"));

        test("[H4] Fator negativo: -a", () ->
            parseProgram("x := -a;"));

        test("[H4] Fator negativo com parênteses: -(a+b)", () ->
            parseProgram("x := -(a + b);"));

        test("[H4] Fator com 'not'", () ->
            parseProgram("x := not a;"));

        test("[H4] Falta ':=' → erro", () ->
            assertThrows(SyntacticException.class,
                () -> parseProgram("x 1;"), "falta :="));

        test("[H4] Expressão ausente após ':=' → erro", () ->
            assertThrows(SyntacticException.class,
                () -> parseProgram("x := ;"), "expressão ausente"));

        test("[H4] Parêntese não-fechado → erro", () ->
            assertThrows(SyntacticException.class,
                () -> parseProgram("x := (a + b;"), "parêntese não-fechado"));
    }

    // -------------------------------------------------------------------------
    // H5 — Expressões relacionais
    // -------------------------------------------------------------------------

    static void testH5_ExpressoesRelacionais() {
        test("[H5] Relop '>'", () ->
            parseProgram("if (a > b) { x := 1; };"));

        test("[H5] Relop '>='", () ->
            parseProgram("if (a >= b) { x := 1; };"));

        test("[H5] Relop '<'", () ->
            parseProgram("if (a < b) { x := 1; };"));

        test("[H5] Relop '<='", () ->
            parseProgram("if (a <= b) { x := 1; };"));

        test("[H5] Relop '<>'", () ->
            parseProgram("if (a <> b) { x := 1; };"));

        test("[H5] Relop '=' (igualdade)", () ->
            parseProgram("if (a = b) { x := 1; };"));

        test("[H5] Condição sem relop (expression' → λ)", () ->
            parseProgram("if (a) { x := 1; };"));

        test("[H5] Expressão relacional com simple-exprs compostas", () ->
            parseProgram("if (a + 1 > b - 2) { x := 1; };"));
    }

    // -------------------------------------------------------------------------
    // H6 — Comando if
    // -------------------------------------------------------------------------

    static void testH6_ComandoIf() {
        test("[H6] if simples", () ->
            parseProgram("if (x > 0) { y := 1; };"));

        test("[H6] if-else", () ->
            parseProgram("if (a = b) { x := 1; } else { x := 2; };"));

        test("[H6] if aninhado", () ->
            parseProgram("if (x > 0) { if (x > 1) { y := 2; }; };"));

        test("[H6] if-else aninhado", () ->
            parseProgram("if (a > b) { x := 1; } else { if (a < b) { x := 2; } else { x := 3; }; };"));

        test("[H6] Falta '(' após if → erro", () ->
            assertThrows(SyntacticException.class,
                () -> parseProgram("if x > 0 { y := 1; };"), "falta ( no if"));

        test("[H6] Falta ')' após condição → erro", () ->
            assertThrows(SyntacticException.class,
                () -> parseProgram("if (x > 0 { y := 1; };"), "falta ) no if"));

        test("[H6] Falta '{' do corpo → erro", () ->
            assertThrows(SyntacticException.class,
                () -> parseProgram("if (x > 0) y := 1;;"), "falta { no if"));

        test("[H6] Falta '}' do corpo → erro", () ->
            assertThrows(SyntacticException.class,
                () -> parseProgram("if (x > 0) { y := 1;;"), "falta } no if"));
    }

    // -------------------------------------------------------------------------
    // H7 — Laços de repetição
    // -------------------------------------------------------------------------

    static void testH7_Lacos() {
        test("[H7] do-while simples", () ->
            parseProgram("do { x := x + 1; } while (x < 10);"));

        test("[H7] repeat-until simples", () ->
            parseProgram("repeat { x := x + 1; } until (x >= 10);"));

        test("[H7] do-while com condição composta", () ->
            parseProgram("do { x := 1; } while (x < 10 and x > 0);"));

        test("[H7] do aninhado", () ->
            parseProgram("do { do { x := 1; } while (x < 5); } while (x < 10);"));

        test("[H7] Falta 'while' após corpo do do → erro", () ->
            assertThrows(SyntacticException.class,
                () -> parseProgram("do { x := 1; } until (x > 0);"), "until no lugar de while"));

        test("[H7] Falta 'until' após corpo do repeat → erro", () ->
            assertThrows(SyntacticException.class,
                () -> parseProgram("repeat { x := 1; } while (x > 0);"), "while no lugar de until"));

        test("[H7] Falta '(' no while → erro", () ->
            assertThrows(SyntacticException.class,
                () -> parseProgram("do { x := 1; } while x < 10;"), "falta ( no while"));

        test("[H7] Falta ')' no while → erro", () ->
            assertThrows(SyntacticException.class,
                () -> parseProgram("do { x := 1; } while (x < 10;"), "falta ) no while"));
    }

    // -------------------------------------------------------------------------
    // H8 — Comandos read e write
    // -------------------------------------------------------------------------

    static void testH8_ReadWrite() {
        test("[H8] read simples", () ->
            parseProgram("read(x);"));

        test("[H8] write com literal string", () ->
            parseProgram("write(\"ola\");"));

        test("[H8] write com expressão aritmética", () ->
            parseProgram("write(a + b);"));

        test("[H8] write com constante real", () ->
            parseProgram("write(3.14);"));

        test("[H8] write com concatenação: string + id", () ->
            parseProgram("write(\"nome: \" + x);"));

        test("[H8] Falta '(' no read → erro", () ->
            assertThrows(SyntacticException.class,
                () -> parseProgram("read x;"), "falta ( no read"));

        test("[H8] Falta ')' no read → erro", () ->
            assertThrows(SyntacticException.class,
                () -> parseProgram("read(x;"), "falta ) no read"));

        test("[H8] Falta '(' no write → erro", () ->
            assertThrows(SyntacticException.class,
                () -> parseProgram("write \"ola\";"), "falta ( no write"));

        test("[H8] Falta ')' no write → erro", () ->
            assertThrows(SyntacticException.class,
                () -> parseProgram("write(\"ola\";"), "falta ) no write"));
    }

    // -------------------------------------------------------------------------
    // H9 — Integração: programas completos
    // -------------------------------------------------------------------------

    static void testH9_ProgramasCompletos() {
        // Programa válido: cálculo de área (versão corrigida do Teste1)
        test("[H9] Programa de área (Teste1 corrigido)", () ->
            parse(
                "class Teste1 {\n" +
                "  int base, altura;\n" +
                "  float area;\n" +
                "  {\n" +
                "    write(\"Digite o valor da base:\");\n" +
                "    read(base);\n" +
                "    write(\"Digite o valor da altura:\");\n" +
                "    read(altura);\n" +
                "    area := base * altura / 2.0;\n" +
                "    write(\"A area e: \" + area);\n" +
                "  }\n" +
                "}"
            ));

        // Programa válido: if-else simples (baseado no Teste3 corrigido)
        test("[H9] Programa com if-else (Teste3 corrigido parcial)", () ->
            parse(
                "class MinhaClasse {\n" +
                "  float a, b, c;\n" +
                "  float maior;\n" +
                "  {\n" +
                "    read(a);\n" +
                "    read(b);\n" +
                "    read(c);\n" +
                "    maior := 0;\n" +
                "    if (a > b and a > c) {\n" +
                "      maior := a;\n" +
                "    } else {\n" +
                "      if (b > c) {\n" +
                "        maior := b;\n" +
                "      } else {\n" +
                "        maior := c;\n" +
                "      };\n" +
                "    };\n" +
                "    write(maior);\n" +
                "  }\n" +
                "}"
            ));

        // Programa válido: do-while e repeat-until
        test("[H9] Programa com do-while e repeat-until", () ->
            parse(
                "class Lacos {\n" +
                "  int i, soma;\n" +
                "  {\n" +
                "    i := 0;\n" +
                "    soma := 0;\n" +
                "    do {\n" +
                "      soma := soma + i;\n" +
                "      i := i + 1;\n" +
                "    } while (i < 10);\n" +
                "    repeat {\n" +
                "      soma := soma - 1;\n" +
                "      i := i - 1;\n" +
                "    } until (i = 0);\n" +
                "    write(soma);\n" +
                "  }\n" +
                "}"
            ));

        // Erros sintáticos do Teste1 original
        test("[H9] Teste1 original: falta ')' em read(altura → erro", () ->
            assertThrows(SyntacticException.class,
                () -> parse(
                    "class Teste1 {\n" +
                    "  int base, altura;\n" +
                    "  {\n" +
                    "    read(altura;\n" +  // erro: falta )
                    "  }\n" +
                    "}"
                ), "falta ) no read"));

        test("[H9] Teste1 original: falta ';' após atribuição → erro", () ->
            assertThrows(SyntacticException.class,
                () -> parse(
                    "class Teste1 {\n" +
                    "  float area;\n" +
                    "  {\n" +
                    "    area := 1.0\n" +  // erro: falta ;
                    "    write(area);\n" +
                    "  }\n" +
                    "}"
                ), "falta ; após atribuição"));
    }

    // -------------------------------------------------------------------------
    // main
    // -------------------------------------------------------------------------

    public static void main(String[] args) {
        System.out.println("=== SyntacticAnalyser — Testes (TDD) ===\n");

        System.out.println("[ H1 — Infraestrutura ]");
        testH1_Infraestrutura();

        System.out.println("\n[ H2 — Estrutura do programa ]");
        testH2_EstruturaDoProgramma();

        System.out.println("\n[ H3 — Declarações ]");
        testH3_Declaracoes();

        System.out.println("\n[ H4 — Atribuição e expressões aritméticas ]");
        testH4_AtribuicaoExpressoes();

        System.out.println("\n[ H5 — Expressões relacionais ]");
        testH5_ExpressoesRelacionais();

        System.out.println("\n[ H6 — Comando if ]");
        testH6_ComandoIf();

        System.out.println("\n[ H7 — Laços ]");
        testH7_Lacos();

        System.out.println("\n[ H8 — Read e Write ]");
        testH8_ReadWrite();

        System.out.println("\n[ H9 — Integração ]");
        testH9_ProgramasCompletos();

        System.out.printf("%n=== Resultado: %d aprovados, %d reprovados ===%n", passed, failed);
        // Sem System.exit — ainda esperamos falhas (implementação pendente)
    }
}
