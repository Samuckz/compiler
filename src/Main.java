import analyser.LexicalAnalyser;
import config.Environment;
import models.Tag;
import models.Token;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        File folder = new File("src/resources/inputs");
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".txt"));
        if (files == null || files.length == 0) {
            System.out.println("Nenhum arquivo .txt encontrado em src/resources/inputs");
            return;
        }
        for (File file : files) {
            System.out.println("Testando arquivo: " + file.getName());
            try {
                LexicalAnalyser lexer = new LexicalAnalyser(file.getName());
                Token token;
                do {
                    token = lexer.scan();
                    Environment.put(token, token.getTag());
                } while (token.getTag() != Tag.EOF);
            } catch (Exception e) {
                System.out.println("Erro ao processar " + file.getName() + ": " + e.getMessage());
            }
            Environment.tableOverview();
        }
    }
}