# Compilador — Disciplina de Compiladores

Centro Federal de Educação Tecnológica de Minas Gerais  
Departamento de Computação — Engenharia de Computação  
Prof.ª Kecia Marques — 2026/1

---

## Objetivo

Este repositório contém a implementação de um compilador para uma linguagem de programação definida pela disciplina de Compiladores. O compilador cobre as fases de análise léxica, análise sintática, análise semântica e geração de código (Jasmin/JVM).

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
│   │   ├── Symbol.java                    # Representa um símbolo na tabela
│   │   ├── SymbolTable.java               # Tabela de símbolos com escopo encadeado
│   │   └── Type.java                      # Constantes e regras de tipos
│   ├── codegen/
│   │   └── CodeGenerator.java             # Gerador de código Jasmin (JVM)
│   ├── utils/
│   │   ├── Consts.java                    # Constantes de caracteres
│   │   └── exceptions/
│   │       ├── LexicalException.java      # Exceção de erro léxico
│   │       ├── SyntacticException.java    # Exceção de erro sintático
│   │       └── SemanticException.java     # Exceção de erro semântico
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
├── compiler.jar                           # JAR executável do compilador
└── jasmin.jar                             # Montador Jasmin (converte .j em .class)
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

### Etapa 3 — Analisador Semântico e Geração de Código
Implementado via **Tradução Dirigida pela Sintaxe (SDT)** — as ações semânticas e de geração de código estão embutidas diretamente nos métodos do parser, sem construção de AST separada.

- **Análise semântica:** tabela de símbolos com escopo encadeado, verificação de tipos, detecção de variáveis não declaradas e redeclarações
- **Geração de código:** produz código **Jasmin** (assembly JVM) que é montado para bytecode `.class` executável diretamente pela JVM

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

## Executando o programa gerado

Após a compilação bem-sucedida, o compilador gera um arquivo `.j` (Jasmin assembly) na pasta do projeto. Para executar o programa compilado, siga os passos abaixo no terminal (`cmd.exe`):

**1. Converta o `.j` para `.class` com o Jasmin:**
```
java -jar jasmin.jar Teste6.j
```

**2. Execute o programa gerado:**
```
java Teste6
```

> O Jasmin só é necessário nesse passo intermediário. O `.class` gerado é bytecode JVM padrão e roda diretamente com `java`.

---

## Saída esperada

**Programa válido (com geração de código):**
```
Compilando: Teste6.txt
----------------------------------------
Compilação concluída com sucesso.
Código Jasmin gerado: Teste6.j
Para executar: java -jar jasmin.jar Teste6.j
```

**Programa com erro semântico:**
```
Compilando: Teste3.txt
----------------------------------------
Erro semântico na linha 5: variável 'x' não declarada
Compilação encerrada com erros semânticos.
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
