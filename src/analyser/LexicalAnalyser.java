package analyser;

import models.Num;
import models.Tag;
import models.Token;
import models.Word;
import utils.Consts;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Hashtable;

public class LexicalAnalyser {
    private int currentLine = 1;
    private char ch = Consts.ESPACO;
    private FileReader file;

    private final Hashtable<String, Word> words = new Hashtable<>();

    public LexicalAnalyser(String filename) throws FileNotFoundException {
        String path = Path.of("src", "resources", "inputs", filename).toString();
        try {
            file = new FileReader(path);
        } catch (FileNotFoundException e) {
            System.out.println("File not found at: " + path);
            throw new RuntimeException(e);
        }

        initiateReservedWords();
    }

    private void reserve(Word word) {
        this.words.put(word.getLexeme(), word);
    }

    private void readCh() {
        try {
            int nextChar = this.file.read();
            char response = (char) nextChar;
            this.ch = nextChar == -1 ? Consts.EOF : response;
        } catch (IOException e) {
            System.out.println("Error while reading file: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private boolean readch(char ch) {
        readCh();
        if (this.ch != ch) {
            return false;
        }
        this.ch = Consts.ESPACO;
        return true;

    }

   public Token scan() {
       // desconsidera delimitadores de entrada
       try{
            for(;; readCh()){
                if(ch == Consts.EOF){
                    return new Token(Tag.EOF);
                } else if(ch == ' ' || ch == '\t' || ch == '\r'){
                    continue;
                } else if(ch == '\n'){
                    currentLine++;
                } else {
                    break;
                }
            }
        
        Token token = getToken(ch);
        System.out.println("Line " + currentLine + ": " + token);
        return token;
        } catch (Exception e) {
            System.out.println("Error while reading file: " + e.getMessage());
            throw new RuntimeException(e);
        }   
    }

    private Token getToken(char ch) {
        // TODO: Verfificar se, para novos tokens, é preciso inserir na tabela de simbolos
        if (ch == Consts.EOF) {
            return new Token(Tag.EOF);
        }

        switch (ch) {
            case Consts.E_COMERCIAL:
                return readch(Consts.E_COMERCIAL) ? Word.and : new Token('&');

            case Consts.PIPE:
                return readch(Consts.PIPE) ? Word.or : new Token('|');

            case Consts.IGUAL:
                return readch(Consts.IGUAL) ? Word.eq : new Token('=');

            case Consts.EXCLAMACAO:
                return readch(Consts.IGUAL) ? Word.ne : new Token('!');

            case Consts.MAIOR:
                return readch(Consts.IGUAL) ? Word.ge : new Token('>');

            case Consts.MENOR:
                return readch(Consts.IGUAL) ? Word.le : new Token('<');
        }

        // Se não começar com nenhum dos caracteres acima, verifica se é um número
        if (Character.isDigit(ch)) {
            int value = 0;
            do {
                value = 10 * value + Character.digit(ch, 10);
                readCh();
            } while (Character.isDigit(this.ch));
            return new Num(value);
        }

        // Identificadores e palavras reservadas
        if (Character.isLetter(ch)) {
            StringBuilder sb = new StringBuilder();
            do {
                sb.append(this.ch);
                readCh();
            } while (Character.isLetterOrDigit(this.ch));

            String lexeme = sb.toString();
            Word word = words.get(lexeme);
            if (word != null) {
                return word; // Palavra reservada
            }
            word = new Word(lexeme, Tag.ID);
            words.put(lexeme, word); // Armazena o identificador para futuras referências
            return word;
        }

        // Caracteres não especificados
        Token token = new Token(ch);
        this.ch = Consts.ESPACO; // Limpa o caractere lido
        return token;

    }

    private void initiateReservedWords() {
        reserve(new Word("class", Tag.CLASS));
        reserve(new Word("int", Tag.INT));
        reserve(new Word("string", Tag.STRING));
        reserve(new Word("float", Tag.FLOAT));
        reserve(new Word("if", Tag.IF));
        reserve(new Word("else", Tag.ELSE));
        reserve(new Word("do", Tag.DO));
        reserve(new Word("while", Tag.WHILE));
        reserve(new Word("repeat", Tag.REPEAT));
        reserve(new Word("until", Tag.UNTIL));
        reserve(new Word("read", Tag.READ));
        reserve(new Word("write", Tag.WRITE));
        reserve(new Word("not", Tag.NOT));
        reserve(new Word("or", Tag.OR));
        reserve(new Word("and", Tag.AND));

    }

}
