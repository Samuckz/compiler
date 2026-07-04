package config;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private final Map<String, Symbol> table;
    private final SymbolTable parent;
    private final int scopeLevel;

    public SymbolTable(SymbolTable parent) {
        this.table = new HashMap<>();
        this.parent = parent;
        this.scopeLevel = (parent == null) ? 0 : parent.scopeLevel + 1;
    }

    /**
     * Insere um símbolo no escopo atual.
     * Lança SemanticException se o lexema já foi declarado neste escopo.
     */
    public void insert(String lexeme, String category, String type, int line) {
        if (table.containsKey(lexeme)) {
            throw new utils.exceptions.SemanticException(
                "Variável '" + lexeme + "' já declarada neste escopo", line);
        }
        table.put(lexeme, new Symbol(lexeme, category, type, scopeLevel));
    }

    /**
     * Busca um símbolo no escopo atual e nos escopos pai.
     * Retorna null se não encontrado.
     */
    public Symbol lookup(String lexeme) {
        Symbol symbol = table.get(lexeme);
        if (symbol != null) return symbol;
        if (parent != null) return parent.lookup(lexeme);
        return null;
    }

    /**
     * Verifica se o lexema já foi declarado no escopo atual (sem subir na cadeia).
     */
    public boolean isDeclared(String lexeme) {
        return table.containsKey(lexeme);
    }

    public int getScopeLevel() { return scopeLevel; }
    public SymbolTable getParent() { return parent; }

    public void printTable() {
        System.out.println("==============================");
        System.out.println("Tabela de Símbolos (escopo " + scopeLevel + ")");
        System.out.println("------------------------------");
        for (Symbol s : table.values()) {
            System.out.println(s);
        }
        System.out.println("==============================");
    }
}
