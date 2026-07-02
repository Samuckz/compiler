package config;

/**
 * Representação de tipos para análise semântica (SDT).
 *
 * Tipos primitivos da linguagem: INT, FLOAT, STRING
 * Tipos de controle: VOID (comandos sem valor), ERROR (propagação de erro semântico)
 * Tipo lógico: BOOL (resultado de expressões relacionais e condições)
 *
 * Tipos estruturais (ARRAY, POINTER) estão previstos na arquitetura
 * mas não são utilizados pela gramática atual.
 */
public class Type {

    // --- Tipos primitivos da linguagem ---
    public static final String INT    = "int";
    public static final String FLOAT  = "float";
    public static final String STRING = "string";

    // --- Tipo lógico (resultado de relop, usado em condições) ---
    public static final String BOOL   = "bool";

    // --- Tipos de controle ---
    public static final String VOID   = "void";   // comandos corretos retornam void
    public static final String ERROR  = "error";  // propaga erro sem cascata

    // --- Tipos estruturais (reservados para extensão futura) ---
    public static final String CHAR    = "char";
    // array e pointer representados como strings compostas, ex: "array(int)", "pointer(float)"

    private Type() {}

    /**
     * Verifica se dois tipos são compatíveis.
     * A linguagem exige tipos idênticos (sem coerção implícita),
     * exceto que ERROR é compatível com qualquer tipo para evitar cascata.
     */
    public static boolean isCompatible(String t1, String t2) {
        if (t1.equals(ERROR) || t2.equals(ERROR)) return true;
        return t1.equals(t2);
    }

    /**
     * Verifica se o tipo é numérico (aceita operadores aritméticos, exceto %).
     */
    public static boolean isNumeric(String type) {
        return type.equals(INT) || type.equals(FLOAT);
    }

    /**
     * Verifica se o tipo é válido como condição (if, do-while, repeat-until).
     */
    public static boolean isLogical(String type) {
        return type.equals(BOOL) || type.equals(ERROR);
    }

    /**
     * Retorna o tipo resultante da operação de adição/subtração entre dois tipos.
     * Regras:
     *   int + int     = int
     *   float + float = float
     *   int + float   = error  (tipos incompatíveis)
     *   string + string = string (concatenação)
     *   qualquer + error = error
     */
    public static String resultOfAddition(String t1, String t2) {
        if (t1.equals(ERROR) || t2.equals(ERROR)) return ERROR;
        if (t1.equals(t2)) return t1;
        return ERROR;
    }

    /**
     * Retorna o tipo resultante de operações de multiplicação/divisão/módulo.
     * Regras:
     *   int * int     = int
     *   float * float = float
     *   int / int     = float  (divisão entre inteiros resulta em real)
     *   % exige int + int = int (validado externamente)
     *   qualquer * error = error
     */
    public static String resultOfMultiplication(String t1, String t2, String operator) {
        if (t1.equals(ERROR) || t2.equals(ERROR)) return ERROR;
        if (operator.equals("%")) {
            return (t1.equals(INT) && t2.equals(INT)) ? INT : ERROR;
        }
        if (operator.equals("/")) {
            return (isNumeric(t1) && isNumeric(t2)) ? FLOAT : ERROR;
        }
        if (t1.equals(t2) && isNumeric(t1)) return t1;
        return ERROR;
    }

    /**
     * Retorna o tipo de uma expressão relacional (sempre bool, ou error se tipos incompatíveis).
     */
    public static String resultOfRelop(String t1, String t2) {
        if (t1.equals(ERROR) || t2.equals(ERROR)) return ERROR;
        if (t1.equals(t2)) return BOOL;
        return ERROR;
    }
}
