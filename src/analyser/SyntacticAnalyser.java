package analyser;

import config.SymbolTable;
import config.Type;
import models.Tag;
import models.Token;
import models.Word;
import utils.exceptions.SemanticException;
import utils.exceptions.SyntacticException;

public class SyntacticAnalyser {

    private final LexicalAnalyser lexer;
    private Token currentToken;
    private final SymbolTable symbolTable;
    private boolean hasSemanticErrors = false;

    public SyntacticAnalyser(LexicalAnalyser lexer) {
        this.lexer = lexer;
        this.symbolTable = new SymbolTable(null);
        advance();
    }

    // -------------------------------------------------------------------------
    // Infra
    // -------------------------------------------------------------------------

    private void advance() {
        currentToken = lexer.scan();
    }

    private void eat(int tag) {
        if (currentToken.getTag() == tag) {
            advance();
        } else {
            throw new SyntacticException(errorMsg(tag));
        }
    }

    private String errorMsg(int expected) {
        String exp = tokenName(expected);
        String got = tokenName(currentToken.getTag());
        if (currentToken instanceof Word) {
            got = "'" + ((Word) currentToken).getLexeme() + "'";
        }
        return "Erro sintático na linha " + lexer.getCurrentLine()
                + ": esperado " + exp + ", encontrado " + got;
    }

    private String tokenName(int tag) {
        switch (tag) {
            case Tag.CLASS:     return "'class'";
            case Tag.INT:       return "'int'";
            case Tag.FLOAT:     return "'float'";
            case Tag.STRING:    return "'string'";
            case Tag.IF:        return "'if'";
            case Tag.ELSE:      return "'else'";
            case Tag.DO:        return "'do'";
            case Tag.WHILE:     return "'while'";
            case Tag.REPEAT:    return "'repeat'";
            case Tag.UNTIL:     return "'until'";
            case Tag.READ:      return "'read'";
            case Tag.WRITE:     return "'write'";
            case Tag.NOT:       return "'not'";
            case Tag.AND:       return "'and'";
            case Tag.OR:        return "'or'";
            case Tag.ASSIGN:    return "':='";
            case Tag.EQUAL:     return "'='";
            case Tag.GE:        return "'>='";
            case Tag.LE:        return "'<='";
            case Tag.NE:        return "'<>'";
            case Tag.GT:        return "'>'";
            case Tag.LT:        return "'<'";
            case Tag.PLUS:      return "'+'";
            case Tag.MINUS:     return "'-'";
            case Tag.TIMES:     return "'*'";
            case Tag.DIVIDE:    return "'/'";
            case Tag.MOD:       return "'%'";
            case Tag.LBRACE:    return "'{'";
            case Tag.RBRACE:    return "'}'";
            case Tag.LPAREN:    return "'('";
            case Tag.RPAREN:    return "')'";
            case Tag.SEMICOLON: return "';'";
            case Tag.COMMA:     return "','";
            case Tag.ID:        return "identificador";
            case Tag.NUM:       return "constante inteira";
            case Tag.REAL:      return "constante real";
            case Tag.EOF:       return "fim de arquivo";
            default:            return "'" + (char) tag + "'";
        }
    }

    // -------------------------------------------------------------------------
    // Ponto de entrada
    // -------------------------------------------------------------------------

    public void analyse() {
        program();
        eat(Tag.EOF);
    }

    public SymbolTable getSymbolTable() { return symbolTable; }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private boolean isType() {
        int t = currentToken.getTag();
        return t == Tag.INT || t == Tag.FLOAT || t == Tag.STRING;
    }

    private boolean isStmtStart() {
        int t = currentToken.getTag();
        return t == Tag.ID || t == Tag.IF || t == Tag.DO
            || t == Tag.REPEAT || t == Tag.READ || t == Tag.WRITE;
    }

    private boolean isAddop() {
        int t = currentToken.getTag();
        return t == Tag.PLUS || t == Tag.MINUS || t == Tag.OR;
    }

    private boolean isMulop() {
        int t = currentToken.getTag();
        return t == Tag.TIMES || t == Tag.DIVIDE || t == Tag.MOD || t == Tag.AND;
    }

    private boolean isRelop() {
        int t = currentToken.getTag();
        return t == Tag.GT || t == Tag.GE || t == Tag.LT
            || t == Tag.LE || t == Tag.NE || t == Tag.EQUAL;
    }

    // -------------------------------------------------------------------------
    // P1 — program ::= class identifier "{" [decl-list] body "}"
    // -------------------------------------------------------------------------

    private void program() {
        eat(Tag.CLASS);
        eat(Tag.ID);
        eat(Tag.LBRACE);
        if (isType()) declList();
        body();
        eat(Tag.RBRACE);
    }

    // -------------------------------------------------------------------------
    // P2 — decl-list ::= decl ";" { decl ";" }
    // -------------------------------------------------------------------------

    private void declList() {
        decl();
        eat(Tag.SEMICOLON);
        while (isType()) {
            decl();
            eat(Tag.SEMICOLON);
        }
    }

    // -------------------------------------------------------------------------
    // P3 — decl ::= type ident-list
    // -------------------------------------------------------------------------

    private void decl() {
        String typeName = type();
        identList(typeName);
    }

    // -------------------------------------------------------------------------
    // P4 — ident-list ::= identifier { "," identifier }
    // -------------------------------------------------------------------------

    private void identList(String typeName) {
        insertSymbol(typeName);
        while (currentToken.getTag() == Tag.COMMA) {
            eat(Tag.COMMA);
            insertSymbol(typeName);
        }
    }

    private void insertSymbol(String typeName) {
        String lexeme = ((Word) currentToken).getLexeme();
        int line = lexer.getCurrentLine();
        eat(Tag.ID);
        try {
            symbolTable.insert(lexeme, "variable", typeName, line);
        } catch (SemanticException e) {
            System.out.println(e.getMessage());
            hasSemanticErrors = true;
        }
    }

    private String semanticError(String message) {
        System.out.println("Erro semântico na linha " + lexer.getCurrentLine() + ": " + message);
        hasSemanticErrors = true;
        return Type.ERROR;
    }

    public boolean hasSemanticErrors() { return hasSemanticErrors; }

    // -------------------------------------------------------------------------
    // P5 — type ::= int | string | float
    // -------------------------------------------------------------------------

    private String type() {
        switch (currentToken.getTag()) {
            case Tag.INT:    eat(Tag.INT);    return Type.INT;
            case Tag.FLOAT:  eat(Tag.FLOAT);  return Type.FLOAT;
            case Tag.STRING: eat(Tag.STRING); return Type.STRING;
            default: throw new SyntacticException(errorMsg(Tag.INT));
        }
    }

    // -------------------------------------------------------------------------
    // P6 — body ::= "{" stmt-list "}"
    // -------------------------------------------------------------------------

    private void body() {
        eat(Tag.LBRACE);
        stmtList();
        eat(Tag.RBRACE);
    }

    // -------------------------------------------------------------------------
    // P7 — stmt-list ::= stmt ";" { stmt ";" }
    // -------------------------------------------------------------------------

    private void stmtList() {
        stmt();
        eat(Tag.SEMICOLON);
        while (isStmtStart()) {
            stmt();
            eat(Tag.SEMICOLON);
        }
    }

    // -------------------------------------------------------------------------
    // P8 — stmt ::= assign-stmt | if-stmt | do-stmt | repeat-stmt
    //              | read-stmt  | write-stmt
    // -------------------------------------------------------------------------

    private String stmt() {
        switch (currentToken.getTag()) {
            case Tag.ID:     return assignStmt();
            case Tag.IF:     return ifStmt();
            case Tag.DO:     return doStmt();
            case Tag.REPEAT: return repeatStmt();
            case Tag.READ:   return readStmt();
            case Tag.WRITE:  return writeStmt();
            default: throw new SyntacticException(
                "Erro sintático na linha " + lexer.getCurrentLine()
                + ": comando inválido, encontrado " + tokenName(currentToken.getTag()));
        }
    }

    // -------------------------------------------------------------------------
    // P9 — assign-stmt ::= identifier ":=" simple-expr
    // -------------------------------------------------------------------------

    private String assignStmt() {
        String lexeme = ((Word) currentToken).getLexeme();
        int line = lexer.getCurrentLine();
        eat(Tag.ID);
        eat(Tag.ASSIGN);
        String exprType = simpleExpr();

        config.Symbol sym = symbolTable.lookup(lexeme);
        if (sym == null) {
            semanticError("variável '" + lexeme + "' não declarada");
            return Type.ERROR;
        }
        if (!Type.isCompatible(sym.getType(), exprType)) {
            semanticError("tipos incompatíveis na atribuição: variável '" + lexeme
                    + "' é do tipo '" + sym.getType() + "', expressão é do tipo '" + exprType + "'");
            return Type.ERROR;
        }
        return Type.VOID;
    }

    // -------------------------------------------------------------------------
    // P10 — if-stmt ::= if "(" condition ")" "{" stmt-list "}" if-stmt'
    // -------------------------------------------------------------------------

    private String ifStmt() {
        eat(Tag.IF);
        eat(Tag.LPAREN);
        condition();
        eat(Tag.RPAREN);
        eat(Tag.LBRACE);
        stmtList();
        eat(Tag.RBRACE);
        ifStmtPrime();
        return Type.VOID;
    }

    // -------------------------------------------------------------------------
    // P11 — if-stmt' ::= else "{" stmt-list "}" | λ
    // -------------------------------------------------------------------------

    private void ifStmtPrime() {
        switch (currentToken.getTag()) {
            case Tag.ELSE:
                eat(Tag.ELSE);
                eat(Tag.LBRACE);
                stmtList();
                eat(Tag.RBRACE);
                break;
            case Tag.SEMICOLON:
                break; // λ
            default:
                throw new SyntacticException(
                    "Erro sintático na linha " + lexer.getCurrentLine()
                    + ": esperado 'else' ou ';', encontrado "
                    + tokenName(currentToken.getTag()));
        }
    }

    // -------------------------------------------------------------------------
    // P12 — do-stmt ::= do "{" stmt-list "}" do-suffix
    // -------------------------------------------------------------------------

    private String doStmt() {
        eat(Tag.DO);
        eat(Tag.LBRACE);
        stmtList();
        eat(Tag.RBRACE);
        doSuffix();
        return Type.VOID;
    }

    // -------------------------------------------------------------------------
    // P13 — do-suffix ::= while "(" condition ")"
    // -------------------------------------------------------------------------

    private void doSuffix() {
        eat(Tag.WHILE);
        eat(Tag.LPAREN);
        condition();
        eat(Tag.RPAREN);
    }

    // -------------------------------------------------------------------------
    // P14 — repeat-stmt ::= repeat "{" stmt-list "}" stmt-suffix
    // -------------------------------------------------------------------------

    private String repeatStmt() {
        eat(Tag.REPEAT);
        eat(Tag.LBRACE);
        stmtList();
        eat(Tag.RBRACE);
        stmtSuffix();
        return Type.VOID;
    }

    // -------------------------------------------------------------------------
    // P15 — stmt-suffix ::= until "(" condition ")"
    // -------------------------------------------------------------------------

    private void stmtSuffix() {
        eat(Tag.UNTIL);
        eat(Tag.LPAREN);
        condition();
        eat(Tag.RPAREN);
    }

    // -------------------------------------------------------------------------
    // P16 — read-stmt ::= read "(" identifier ")"
    // -------------------------------------------------------------------------

    private String readStmt() {
        eat(Tag.READ);
        eat(Tag.LPAREN);
        String lexeme = ((Word) currentToken).getLexeme();
        eat(Tag.ID);
        if (symbolTable.lookup(lexeme) == null) {
            semanticError("variável '" + lexeme + "' não declarada");
            eat(Tag.RPAREN);
            return Type.ERROR;
        }
        eat(Tag.RPAREN);
        return Type.VOID;
    }

    // -------------------------------------------------------------------------
    // P17 — write-stmt ::= write "(" writable ")"
    // -------------------------------------------------------------------------

    private String writeStmt() {
        eat(Tag.WRITE);
        eat(Tag.LPAREN);
        writable();
        eat(Tag.RPAREN);
        return Type.VOID;
    }

    // -------------------------------------------------------------------------
    // P18 — writable ::= simple-expr
    // -------------------------------------------------------------------------

    private String writable() {
        return simpleExpr();
    }

    // -------------------------------------------------------------------------
    // P19 — condition ::= expression
    // -------------------------------------------------------------------------

    private void condition() {
        String t = expression();
        if (!Type.isLogical(t)) {
            semanticError("condição deve ser do tipo lógico, mas é do tipo '" + t + "'");
        }
    }

    // -------------------------------------------------------------------------
    // P20 — expression ::= simple-expr expression'
    // -------------------------------------------------------------------------

    private String expression() {
        String left = simpleExpr();
        return expressionPrime(left);
    }

    // -------------------------------------------------------------------------
    // P21 — expression' ::= relop simple-expr | λ
    // -------------------------------------------------------------------------

    private String expressionPrime(String leftType) {
        if (isRelop()) {
            relop();
            String right = simpleExpr();
            String result = Type.resultOfRelop(leftType, right);
            if (result.equals(Type.ERROR) && !leftType.equals(Type.ERROR) && !right.equals(Type.ERROR)) {
                semanticError("operador relacional aplicado a tipos incompatíveis: '"
                        + leftType + "' e '" + right + "'");
            }
            return result;
        }
        return leftType; // λ
    }

    // -------------------------------------------------------------------------
    // P22 — simple-expr ::= term simple-expr'
    // -------------------------------------------------------------------------

    private String simpleExpr() {
        String left = term();
        return simpleExprPrime(left);
    }

    // -------------------------------------------------------------------------
    // P23 — simple-expr' ::= addop term simple-expr' | λ
    // -------------------------------------------------------------------------

    private String simpleExprPrime(String leftType) {
        if (isAddop()) {
            String op = addop();
            String right = term();
            String result;
            if (op.equals("or")) {
                if (!leftType.equals(Type.BOOL) && !leftType.equals(Type.ERROR)) {
                    semanticError("operador 'or' requer operandos lógicos, mas encontrou '" + leftType + "'");
                }
                if (!right.equals(Type.BOOL) && !right.equals(Type.ERROR)) {
                    semanticError("operador 'or' requer operandos lógicos, mas encontrou '" + right + "'");
                }
                result = Type.BOOL;
            } else {
                result = Type.resultOfAddition(leftType, right);
                if (result.equals(Type.ERROR) && !leftType.equals(Type.ERROR) && !right.equals(Type.ERROR)) {
                    semanticError("operador '" + op + "' incompatível com tipos '"
                            + leftType + "' e '" + right + "'");
                }
            }
            return simpleExprPrime(result);
        }
        return leftType; // λ
    }

    // -------------------------------------------------------------------------
    // P24 — term ::= factor-a term'
    // -------------------------------------------------------------------------

    private String term() {
        String left = factorA();
        return termPrime(left);
    }

    // -------------------------------------------------------------------------
    // P25 — term' ::= mulop factor-a term' | λ
    // -------------------------------------------------------------------------

    private String termPrime(String leftType) {
        if (isMulop()) {
            String op = mulop();
            String right = factorA();
            String result;
            if (op.equals("and")) {
                if (!leftType.equals(Type.BOOL) && !leftType.equals(Type.ERROR)) {
                    semanticError("operador 'and' requer operandos lógicos, mas encontrou '" + leftType + "'");
                }
                if (!right.equals(Type.BOOL) && !right.equals(Type.ERROR)) {
                    semanticError("operador 'and' requer operandos lógicos, mas encontrou '" + right + "'");
                }
                result = Type.BOOL;
            } else {
                result = Type.resultOfMultiplication(leftType, right, op);
                if (result.equals(Type.ERROR) && !leftType.equals(Type.ERROR) && !right.equals(Type.ERROR)) {
                    semanticError("operador '" + op + "' incompatível com tipos '"
                            + leftType + "' e '" + right + "'");
                }
            }
            return termPrime(result);
        }
        return leftType; // λ
    }

    // -------------------------------------------------------------------------
    // P26 — factor-a ::= factor | not factor | "-" factor
    // -------------------------------------------------------------------------

    private String factorA() {
        switch (currentToken.getTag()) {
            case Tag.NOT: {
                eat(Tag.NOT);
                String t = factor();
                if (!t.equals(Type.BOOL) && !t.equals(Type.ERROR)) {
                    return semanticError("operador 'not' requer tipo lógico, mas encontrou '" + t + "'");
                }
                return Type.BOOL;
            }
            case Tag.MINUS: {
                eat(Tag.MINUS);
                String t = factor();
                if (!Type.isNumeric(t) && !t.equals(Type.ERROR)) {
                    return semanticError("operador '-' unário requer tipo numérico, mas encontrou '" + t + "'");
                }
                return t;
            }
            default:
                return factor();
        }
    }

    // -------------------------------------------------------------------------
    // P27 — factor ::= identifier | constant | "(" expression ")"
    // -------------------------------------------------------------------------

    private String factor() {
        switch (currentToken.getTag()) {
            case Tag.ID: {
                String lexeme = ((Word) currentToken).getLexeme();
                int line = lexer.getCurrentLine();
                eat(Tag.ID);
                config.Symbol sym = symbolTable.lookup(lexeme);
                if (sym == null) {
                    return semanticError("variável '" + lexeme + "' não declarada");
                }
                return sym.getType();
            }
            case Tag.NUM:
                eat(Tag.NUM);
                return Type.INT;
            case Tag.REAL:
                eat(Tag.REAL);
                return Type.FLOAT;
            case Tag.STRING:
                eat(Tag.STRING);
                return Type.STRING;
            case Tag.LPAREN: {
                eat(Tag.LPAREN);
                String t = expression();
                eat(Tag.RPAREN);
                return t;
            }
            default:
                throw new SyntacticException(
                    "Erro sintático na linha " + lexer.getCurrentLine()
                    + ": fator inválido, encontrado "
                    + tokenName(currentToken.getTag()));
        }
    }

    // -------------------------------------------------------------------------
    // P28 — relop ::= ">" | ">=" | "<" | "<=" | "<>" | "="
    // -------------------------------------------------------------------------

    private void relop() {
        switch (currentToken.getTag()) {
            case Tag.GT:    eat(Tag.GT);    break;
            case Tag.GE:    eat(Tag.GE);    break;
            case Tag.LT:    eat(Tag.LT);    break;
            case Tag.LE:    eat(Tag.LE);    break;
            case Tag.NE:    eat(Tag.NE);    break;
            case Tag.EQUAL: eat(Tag.EQUAL); break;
            default: throw new SyntacticException(errorMsg(Tag.GT));
        }
    }

    // -------------------------------------------------------------------------
    // P29 — addop ::= "+" | "-" | or
    // -------------------------------------------------------------------------

    private String addop() {
        switch (currentToken.getTag()) {
            case Tag.PLUS:  eat(Tag.PLUS);  return "+";
            case Tag.MINUS: eat(Tag.MINUS); return "-";
            case Tag.OR:    eat(Tag.OR);    return "or";
            default: throw new SyntacticException(errorMsg(Tag.PLUS));
        }
    }

    // -------------------------------------------------------------------------
    // P30 — mulop ::= "*" | "/" | "%" | and
    // -------------------------------------------------------------------------

    private String mulop() {
        switch (currentToken.getTag()) {
            case Tag.TIMES:  eat(Tag.TIMES);  return "*";
            case Tag.DIVIDE: eat(Tag.DIVIDE); return "/";
            case Tag.MOD:    eat(Tag.MOD);    return "%";
            case Tag.AND:    eat(Tag.AND);    return "and";
            default: throw new SyntacticException(errorMsg(Tag.TIMES));
        }
    }
}
