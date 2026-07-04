package config;

public class Symbol {
    private final String lexeme;
    private final String category;
    private String type;
    private final int scope;

    public Symbol(String lexeme, String category, String type, int scope) {
        this.lexeme = lexeme;
        this.category = category;
        this.type = type;
        this.scope = scope;
    }

    public String getLexeme()   { return lexeme; }
    public String getCategory() { return category; }
    public String getType()     { return type; }
    public int    getScope()    { return scope; }

    public void setType(String type) { this.type = type; }

    @Override
    public String toString() {
        return String.format("Symbol{lexeme='%s', category='%s', type='%s', scope=%d}",
                lexeme, category, type, scope);
    }
}
