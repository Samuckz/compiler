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
    // Produções — serão implementadas nas histórias seguintes
    // -------------------------------------------------------------------------

    private void program() {
        throw new SyntacticException(
            "Erro sintático na linha " + lexer.getCurrentLine()
            + ": esperado 'class', encontrado " + tokenName(currentToken.getTag()));
    }
}
