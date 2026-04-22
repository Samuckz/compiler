package analyser;

import models.Decimal;
import models.Num;
import models.Tag;
import models.Token;
import models.Word;
import utils.Consts;
import utils.exceptions.LexicalException;

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
            throw new LexicalException(e.getMessage());
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
            throw new LexicalException(e.getMessage());
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
        try {
            for (;; readCh()) {
                if (ch == Consts.EOF) {
                    return new Token(Tag.EOF);
                } else if (ch == ' ' || ch == '\t' || ch == '\r') {
                    continue;
                } else if (ch == '\n') {
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
            throw new LexicalException(e.getMessage());
        }
    }

    private Token getToken(char ch) {
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

            case Consts.DOIS_PONTOS:
                return readch(Consts.IGUAL) ? Word.eq : new Token(':');

        }

        // se igual a "//" pular linha
        if (this.ch == Consts.BARRA) {
            readCh();
            if (this.ch == Consts.BARRA) {
                // Comentário de linha encontrado, ignora até o final da linha
                while (this.ch != Consts.NEWLINE && this.ch != Consts.EOF) {
                    readCh();
                }
                return scan(); // Após terminar o comentário, chama scan novamente para obter o próximo token
            }
            if (this.ch == Consts.ASTERISCO) {
                // Comentário encontrado, ignora até o fim do comentário
                while (true) {
                    if (this.ch == Consts.EOF) {
                        throw new LexicalException("Unterminated comment at line " + currentLine);
                    }
                    if (this.ch == Consts.FIM_COMENTARIO.charAt(0)) {
                        readCh();
                        if (this.ch == Consts.FIM_COMENTARIO.charAt(1)) {
                            readCh(); // Consome o segundo caractere do fim do comentário
                            break; // Sai do loop de comentário
                        }
                    } else {
                        readCh(); // Continua lendo dentro do comentário
                    }
                }
                return scan(); // Após terminar o comentário, chama scan novamente para obter o próximo token
            } else {
                return new Token(Consts.INICIO_COMENTARIO.charAt(0)); // Retorna o caractere '/' se não for um
            }
        }


        // Valida String
        if (ch == Consts.ASPAS) {
            StringBuilder sb = new StringBuilder();
            readCh();
            while (this.ch != Consts.ASPAS && this.ch != Consts.EOF) {
                sb.append(this.ch);
                readCh();
            }
            if (this.ch == Consts.ASPAS) {
                readCh(); // Consome a aspa de fechamento
                return new Word(sb.toString(), Tag.STRING);
            } else {
                throw new LexicalException("Unterminated string literal at line " + currentLine);
            }
        }

        // Se não começar com nenhum dos caracteres acima, verifica se é um número
        if (Character.isDigit(ch)) {
            int integerPart = 0;

            // Lê a parte inteira
            do {
                integerPart = 10 * integerPart + Character.digit(ch, 10);
                readCh();
            } while (Character.isDigit(this.ch));

            // Verifica se existe parte decimal
            if (this.ch == '.') {
                readCh(); // Consome o ponto '.'

                // Após o ponto, DEVE haver pelo menos um dígito
                if (!Character.isDigit(this.ch)) {
                    throw new NumberFormatException(
                            "Número mal formatado: esperado dígito após '.', mas encontrou '" + this.ch + "'");
                }

                double decimalPart = 0.0;
                double multiplier = 0.1;

                // Lê a parte decimal
                do {
                    decimalPart += Character.digit(ch, 10) * multiplier;
                    multiplier *= 0.1;
                    readCh();
                } while (Character.isDigit(this.ch));

                return new Decimal(integerPart + decimalPart);
            }

            return new Num(integerPart);
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
