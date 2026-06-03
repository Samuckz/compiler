import analyser.LexicalAnalyser;
import config.Environment;
import models.Tag;
import models.Token;
import utils.Consts;

import java.io.File;
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
        System.out.println("Executando analisador léxico para: " + fileName);
        try {
            LexicalAnalyser lexer = new LexicalAnalyser(fileName);
            Token token;
            do {
                token = lexer.scan();
                System.out.println("Line " + lexer.getCurrentLine() + ": " + token);
                if (token.getTag().equals(Tag.ID)) {
                    Environment.put(token, token.getTag());
                }
            } while (token.getTag() != Tag.EOF);
        } catch (Exception e) {
            System.out.println("Erro ao processar " + fileName + ": " + e.getMessage());
        }
        Environment.tableOverview();
    }
}