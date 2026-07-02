import analyser.LexicalAnalyser;
import analyser.SyntacticAnalyser;
import utils.exceptions.LexicalException;
import utils.exceptions.SemanticException;
import utils.exceptions.SyntacticException;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Digite um número de 1 a 6 para escolher o teste a ser executado:");
        System.out.println("1 - Teste1.txt");
        System.out.println("2 - Teste2.txt");
        System.out.println("3 - Teste3.txt");
        System.out.println("4 - Teste4.txt");
        System.out.println("5 - Teste5.txt");
        System.out.println("6 - Teste6.txt");
        System.out.print("Sua escolha: ");
        int escolha = scanner.nextInt();
        if (escolha < 1 || escolha > 6) {
            System.out.println("Opção inválida. Encerrando.");
            return;
        }
        String fileName = "Teste" + escolha + ".txt";
        System.out.println("Compilando: " + fileName);
        System.out.println("----------------------------------------");
        try {
            LexicalAnalyser lexer = new LexicalAnalyser(fileName);
            SyntacticAnalyser parser = new SyntacticAnalyser(lexer);
            parser.analyse();
            if (parser.hasSemanticErrors()) {
                System.out.println("----------------------------------------");
                System.out.println("Compilação encerrada com erros semânticos.");
            } else {
                System.out.println("Compilação concluída com sucesso.");
            }
        } catch (LexicalException e) {
            System.out.println(e.getMessage());
            System.out.println("Compilação encerrada com erros léxicos.");
        } catch (SyntacticException e) {
            System.out.println(e.getMessage());
            System.out.println("Compilação encerrada com erros sintáticos.");
        }
    }
}