package config;

import java.util.Hashtable;

import models.Token;

public class Environment {
    private static Hashtable<Token, Integer> table = new Hashtable<>();
    private static Environment prev;

    public Environment(Environment prev) {
        Environment.prev = prev;
        table = new Hashtable<>();
    }

    public static void put(Token token, Integer value) {
        table.put(token, value);
    }

    public static Integer get(Token token) {
        Integer value = table.get(token);
        if (value != null) {
            return value;
        } else if (prev != null) {
            return get(token);
        }
        
        return null;

    }

    public static void tableOverview(){
        System.out.println("******************************");
        System.out.println("Tabela de simbolos");
        System.out.println("------------------");

        for (Token token: table.keySet()) {
            System.out.println("Token: " + token.getTag() + " | Valor: " + token);
        }

        System.out.println("******************************");
    }
}
