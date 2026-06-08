# Compilador — Disciplina de Compiladores

Centro Federal de Educação Tecnológica de Minas Gerais  
Departamento de Computação — Engenharia de Computação  
Prof.ª Kecia Marques — 2026/1

---

## Objetivo

Este repositório contém a implementação de um compilador para uma linguagem de programação definida pela disciplina de Compiladores. O compilador é desenvolvido em etapas ao longo do semestre, cobrindo as fases de análise léxica, análise sintática e, futuramente, análise semântica e geração de código.

---

## Estrutura do código

```
compiler/
├── src/
│   ├── Main.java                          # Ponto de entrada da aplicação
│   ├── analyser/
│   │   ├── LexicalAnalyser.java           # Analisador léxico
│   │   └── SyntacticAnalyser.java         # Analisador sintático
│   ├── models/
│   │   ├── Tag.java                       # Constantes de tipos de token
│   │   ├── Token.java                     # Classe base dos tokens
│   │   ├── Word.java                      # Tokens de palavras e operadores
│   │   ├── Num.java                       # Tokens de constantes inteiras
│   │   └── Decimal.java                   # Tokens de constantes reais
│   ├── config/
│   │   └── Environment.java               # Tabela de símbolos
│   ├── utils/
│   │   ├── Consts.java                    # Constantes de caracteres
│   │   └── exceptions/
│   │       ├── LexicalException.java      # Exceção de erro léxico
│   │       └── SyntacticException.java    # Exceção de erro sintático
│   ├── resources/
│   │   └── inputs/
│   │       ├── Teste1.txt                 # Arquivo de teste 1
│   │       ├── Teste2.txt                 # Arquivo de teste 2
│   │       ├── Teste3.txt                 # Arquivo de teste 3
│   │       ├── Teste4.txt                 # Arquivo de teste 4
│   │       ├── Teste5.txt                 # Arquivo de teste 5
│   │       └── Teste6.txt                 # Arquivo de teste 6
│   └── test/
│       └── SyntacticAnalyserTest.java     # Testes automatizados do parser
├── docs/
│   └── sintatico/
│       ├── tabela-transicoes.md           # Tabela de transições do parser
│       └── historias.md                   # Histórias de desenvolvimento (TDD)
├── out/                                   # Classes compiladas (gerado automaticamente)
└── compiler.jar                           # JAR executável
```

---

## Analisadores implementados

### Etapa 1 — Analisador Léxico
Responsável por ler o arquivo de entrada caractere a caractere e produzir a sequência de tokens. Reconhece:
- Palavras reservadas: `class`, `int`, `float`, `string`, `if`, `else`, `do`, `while`, `repeat`, `until`, `read`, `write`, `not`, `and`, `or`
- Identificadores e constantes (inteiras, reais e literais string)
- Operadores simples (`+`, `-`, `*`, `/`, `%`, `=`, `<`, `>`) e compostos (`:=`, `>=`, `<=`, `<>`, `&&`, `||`)
- Comentários de linha (`//`) e de bloco (`/* */`)

### Etapa 2 — Analisador Sintático
Responsável por verificar se a sequência de tokens respeita a gramática da linguagem. Implementado como um **parser descendente recursivo (LL)**, com um método para cada não-terminal da gramática. Suporta:
- Declarações de variáveis (`int`, `float`, `string`)
- Comandos de atribuição (`:=`)
- Estruturas condicionais (`if`, `if-else`)
- Estruturas de repetição (`do-while`, `repeat-until`)
- Comandos de entrada e saída (`read`, `write`)
- Expressões aritméticas e relacionais com precedência correta

---

## Como rodar

### Opção 1 — JAR executável (recomendado)

Requisito: Java instalado na máquina.

```bash
java -jar compiler.jar
```

Ao executar, o programa exibe um menu para escolha do arquivo de teste:

```
Digite um número de 1 a 6 para escolher o teste a ser executado:
1 - Teste1.txt
2 - Teste2.txt
...
Sua escolha:
```

### Opção 2 — Compilando e executando pelo terminal

A partir do diretório raiz do projeto:

```bash
# 1. Compilar os fontes
find src -name "*.java" | xargs javac -d out

# 2. Executar
java -cp out Main
```

### Opção 3 — Executando os testes automatizados

```bash
java -cp out test.SyntacticAnalyserTest
```

---

## Saída esperada

**Programa válido:**
```
Compilando: Teste6.txt
----------------------------------------
Compilação concluída com sucesso.
```

**Programa com erro léxico:**
```
Compilando: Teste2.txt
----------------------------------------
Erro léxico: Unterminated comment at line 13
```

**Programa com erro sintático:**
```
Compilando: Teste1.txt
----------------------------------------
Erro sintático: Erro sintático na linha 2: esperado identificador, encontrado constante inteira
```
