package analyser;

import models.Tag;
import models.Token;
import models.Word;
import utils.exceptions.SyntacticException;

public class SyntacticAnalyser {

    private final LexicalAnalyser lexer;
    private Token currentToken;

    public SyntacticAnalyser(LexicalAnalyser lexer) {
        this.lexer = lexer;
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
        type();
        identList();
    }

    // -------------------------------------------------------------------------
    // P4 — ident-list ::= identifier { "," identifier }
    // -------------------------------------------------------------------------

    private void identList() {
        eat(Tag.ID);
        while (currentToken.getTag() == Tag.COMMA) {
            eat(Tag.COMMA);
            eat(Tag.ID);
        }
    }

    // -------------------------------------------------------------------------
    // P5 — type ::= int | string | float
    // -------------------------------------------------------------------------

    private void type() {
        switch (currentToken.getTag()) {
            case Tag.INT:    eat(Tag.INT);    break;
            case Tag.FLOAT:  eat(Tag.FLOAT);  break;
            case Tag.STRING: eat(Tag.STRING); break;
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

    private void stmt() {
        switch (currentToken.getTag()) {
            case Tag.ID:     assignStmt();  break;
            case Tag.IF:     ifStmt();      break;
            case Tag.DO:     doStmt();      break;
            case Tag.REPEAT: repeatStmt();  break;
            case Tag.READ:   readStmt();    break;
            case Tag.WRITE:  writeStmt();   break;
            default: throw new SyntacticException(
                "Erro sintático na linha " + lexer.getCurrentLine()
                + ": comando inválido, encontrado " + tokenName(currentToken.getTag()));
        }
    }

    // -------------------------------------------------------------------------
    // P9 — assign-stmt ::= identifier ":=" simple-expr
    // -------------------------------------------------------------------------

    private void assignStmt() {
        eat(Tag.ID);
        eat(Tag.ASSIGN);
        simpleExpr();
    }

    // -------------------------------------------------------------------------
    // P10 — if-stmt ::= if "(" condition ")" "{" stmt-list "}" if-stmt'
    // -------------------------------------------------------------------------

    private void ifStmt() {
        eat(Tag.IF);
        eat(Tag.LPAREN);
        condition();
        eat(Tag.RPAREN);
        eat(Tag.LBRACE);
        stmtList();
        eat(Tag.RBRACE);
        ifStmtPrime();
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

    private void doStmt() {
        eat(Tag.DO);
        eat(Tag.LBRACE);
        stmtList();
        eat(Tag.RBRACE);
        doSuffix();
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

    private void repeatStmt() {
        eat(Tag.REPEAT);
        eat(Tag.LBRACE);
        stmtList();
        eat(Tag.RBRACE);
        stmtSuffix();
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

    private void readStmt() {
        eat(Tag.READ);
        eat(Tag.LPAREN);
        eat(Tag.ID);
        eat(Tag.RPAREN);
    }

    // -------------------------------------------------------------------------
    // P17 — write-stmt ::= write "(" writable ")"
    // -------------------------------------------------------------------------

    private void writeStmt() {
        eat(Tag.WRITE);
        eat(Tag.LPAREN);
        writable();
        eat(Tag.RPAREN);
    }

    // -------------------------------------------------------------------------
    // P18 — writable ::= simple-expr
    // -------------------------------------------------------------------------

    private void writable() {
        simpleExpr();
    }

    // -------------------------------------------------------------------------
    // P19 — condition ::= expression
    // -------------------------------------------------------------------------

    private void condition() {
        expression();
    }

    // -------------------------------------------------------------------------
    // P20 — expression ::= simple-expr expression'
    // -------------------------------------------------------------------------

    private void expression() {
        simpleExpr();
        expressionPrime();
    }

    // -------------------------------------------------------------------------
    // P21 — expression' ::= relop simple-expr | λ
    // -------------------------------------------------------------------------

    private void expressionPrime() {
        if (isRelop()) {
            relop();
            simpleExpr();
        }
        // λ: FOLLOW = { ) }
    }

    // -------------------------------------------------------------------------
    // P22 — simple-expr ::= term simple-expr'
    // -------------------------------------------------------------------------

    private void simpleExpr() {
        term();
        simpleExprPrime();
    }

    // -------------------------------------------------------------------------
    // P23 — simple-expr' ::= addop term simple-expr' | λ
    // -------------------------------------------------------------------------

    private void simpleExprPrime() {
        if (isAddop()) {
            addop();
            term();
            simpleExprPrime();
        }
        // λ: FOLLOW = { ), ;, relops }
    }

    // -------------------------------------------------------------------------
    // P24 — term ::= factor-a term'
    // -------------------------------------------------------------------------

    private void term() {
        factorA();
        termPrime();
    }

    // -------------------------------------------------------------------------
    // P25 — term' ::= mulop factor-a term' | λ
    // -------------------------------------------------------------------------

    private void termPrime() {
        if (isMulop()) {
            mulop();
            factorA();
            termPrime();
        }
        // λ: FOLLOW = { ), ;, addops, relops }
    }

    // -------------------------------------------------------------------------
    // P26 — factor-a ::= factor | not factor | "-" factor
    // -------------------------------------------------------------------------

    private void factorA() {
        switch (currentToken.getTag()) {
            case Tag.NOT:
                eat(Tag.NOT);
                factor();
                break;
            case Tag.MINUS:
                eat(Tag.MINUS);
                factor();
                break;
            default:
                factor();
        }
    }

    // -------------------------------------------------------------------------
    // P27 — factor ::= identifier | constant | "(" expression ")"
    // -------------------------------------------------------------------------

    private void factor() {
        switch (currentToken.getTag()) {
            case Tag.ID:
                eat(Tag.ID);
                break;
            case Tag.NUM:
                eat(Tag.NUM);
                break;
            case Tag.REAL:
                eat(Tag.REAL);
                break;
            case Tag.STRING:
                eat(Tag.STRING);
                break;
            case Tag.LPAREN:
                eat(Tag.LPAREN);
                expression();
                eat(Tag.RPAREN);
                break;
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

    private void addop() {
        switch (currentToken.getTag()) {
            case Tag.PLUS:  eat(Tag.PLUS);  break;
            case Tag.MINUS: eat(Tag.MINUS); break;
            case Tag.OR:    eat(Tag.OR);    break;
            default: throw new SyntacticException(errorMsg(Tag.PLUS));
        }
    }

    // -------------------------------------------------------------------------
    // P30 — mulop ::= "*" | "/" | "%" | and
    // -------------------------------------------------------------------------

    private void mulop() {
        switch (currentToken.getTag()) {
            case Tag.TIMES:  eat(Tag.TIMES);  break;
            case Tag.DIVIDE: eat(Tag.DIVIDE); break;
            case Tag.MOD:    eat(Tag.MOD);    break;
            case Tag.AND:    eat(Tag.AND);    break;
            default: throw new SyntacticException(errorMsg(Tag.TIMES));
        }
    }
}
