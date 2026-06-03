# Histórias de Usuário — Analisador Sintático

> Cada história é independente e compilável. A ordem respeita dependências mínimas:
> infraestrutura → estrutura do programa → declarações → comandos simples → expressões → comandos compostos → integração.

---

## H1 — Infraestrutura do parser

**Descrição:** Como desenvolvedor, preciso da estrutura base do parser para que as demais histórias possam ser implementadas sobre ela.

**Critérios de aceitação:**
- Existe a classe `SyntacticAnalyser` com construtor `(LexicalAnalyser)`
- Existe o método `analyse()` como ponto de entrada público
- Existe o método privado `eat(int tag)` que avança o token se ele bater, ou lança `SyntacticException` se não bater
- Existe o método privado `advance()` que chama `lexer.scan()`
- Existe `SyntacticException` com mensagem contendo o número da linha
- `analyse()` encerra verificando `EOF`

**Casos de teste TDD:**
- `parse("")` → lança `SyntacticException` (EOF sem `class`)
- `eat` com token correto → avança sem exceção
- `eat` com token errado → lança `SyntacticException` com número de linha

---

## H2 — Estrutura do programa

**Descrição:** Como compilador, preciso reconhecer a estrutura mínima de um programa (`class id { body }`) para que qualquer programa válido possa ser analisado.

**Depende de:** H1, H6 parcial (body)

**Critérios de aceitação:**
- `program()` reconhece `class identifier "{" body "}"`
- `body()` reconhece `"{" stmt-list "}"`
- A seção `[decl-list]` é opcional: se o próximo token for `int`/`float`/`string`, `declList()` é chamada; caso contrário, é ignorada
- Programa sem `class` → `SyntacticException`
- Programa sem identificador após `class` → `SyntacticException`
- Programa sem `{` de abertura do body → `SyntacticException`
- Programa sem `}` de fechamento → `SyntacticException`

**Casos de teste TDD:**
- `class Foo { { } }` → sucesso *(programa mínimo: body vazio não é válido na gramática estrita, ver H3)*
- `Foo { { } }` → erro (falta `class`)
- `class { { } }` → erro (falta identificador)
- `class Foo { }` → erro (falta body)

---

## H3 — Declarações de variáveis

**Descrição:** Como compilador, preciso reconhecer declarações de variáveis (`int x, y; float z;`) para validar a seção de declarações de um programa.

**Depende de:** H2

**Critérios de aceitação:**
- `declList()` reconhece uma ou mais declarações separadas por `;`
- `decl()` reconhece `type ident-list`
- `type()` aceita `int`, `float` e `string`
- `identList()` reconhece um ou mais identificadores separados por `,`
- Tipo inválido → `SyntacticException`
- Declaração sem identificador → `SyntacticException`
- `decl-list` sem `;` ao final → `SyntacticException`

**Casos de teste TDD:**
- `class Foo { int x; { ... } }` → sucesso
- `class Foo { int x, y, z; float a; { ... } }` → sucesso
- `class Foo { string nome; int idade; { ... } }` → sucesso (múltiplos tipos)
- `class Foo { x; { ... } }` → erro (tipo ausente)
- `class Foo { int ; { ... } }` → erro (identificador ausente)
- `class Foo { int x { ... } }` → erro (`;` ausente após declaração)

---

## H4 — Comando de atribuição e expressões aritméticas

**Descrição:** Como compilador, preciso reconhecer atribuições (`x := expr`) e expressões aritméticas completas (com `+`, `-`, `*`, `/`, `%`, parênteses e fator negativo).

**Depende de:** H3

**Critérios de aceitação:**
- `assignStmt()` reconhece `identifier ":=" simple-expr`
- `simpleExpr()`, `term()`, `factorA()`, `factor()` e suas primas reconhecem expressões aritméticas
- `factor` aceita `identifier`, constante inteira, constante real, literal string e expressão parentesizada
- `factor-a` aceita `not factor` e `"-" factor` além de `factor`
- `simple-expr'` e `term'` consomem zero ou mais repetições dos respectivos operadores
- Atribuição sem `:=` → `SyntacticException`
- Expressão vazia após `:=` → `SyntacticException`

**Casos de teste TDD:**
- `x := 1;` → sucesso
- `x := a + b;` → sucesso
- `x := a * b + c / d;` → sucesso (precedência via gramática)
- `x := (a + b) * c;` → sucesso (parênteses)
- `x := -a;` → sucesso (fator negativo)
- `x := not a;` → sucesso
- `x := 3.14;` → sucesso (real)
- `x := "texto";` → sucesso (string)
- `x 1;` → erro (falta `:=`)
- `x := ;` → erro (expressão ausente)
- `x := (a + b;` → erro (parêntese não-fechado)

---

## H5 — Expressões relacionais

**Descrição:** Como compilador, preciso reconhecer expressões com operadores relacionais (`>`, `>=`, `<`, `<=`, `<>`, `=`) para validar condições.

**Depende de:** H4

**Critérios de aceitação:**
- `expression()` reconhece `simple-expr expression'`
- `expression'` reconhece `relop simple-expr` ou `λ`
- `relop()` consome qualquer um dos seis operadores relacionais
- `condition()` delega para `expression()`
- Todos os seis relops são reconhecidos sem erro
- Token inválido em posição de relop → `SyntacticException` OU λ (ver FOLLOW)

**Casos de teste TDD:**
- `a > b` como condição → sucesso
- `a >= b` → sucesso
- `a < b` → sucesso
- `a <= b` → sucesso
- `a <> b` → sucesso
- `a = b` → sucesso
- `a + 1 > b - 2` → sucesso (relop entre simple-exprs)
- `a` sozinho como condição (sem relop) → sucesso (expression' → λ)

---

## H6 — Comando if

**Descrição:** Como compilador, preciso reconhecer o comando `if` com e sem `else` para validar estruturas condicionais.

**Depende de:** H5

**Critérios de aceitação:**
- `ifStmt()` reconhece `if "(" condition ")" "{" stmt-list "}" if-stmt'`
- `ifStmtPrime()` reconhece `else "{" stmt-list "}"` ou `λ`
- `if` sem `(` → `SyntacticException`
- `if` sem `)` após condição → `SyntacticException`
- `if` sem `{` → `SyntacticException`
- `if` sem `}` → `SyntacticException`
- `else` sem `{` → `SyntacticException`

**Casos de teste TDD:**
- `if (x > 0) { y := 1; }` → sucesso
- `if (a = b) { x := 1; } else { x := 2; }` → sucesso
- `if (x > 0) { if (x > 1) { y := 2; }; }` → sucesso (if aninhado)
- `if x > 0 { y := 1; }` → erro (falta `(`)
- `if (x > 0 { y := 1; }` → erro (falta `)`)
- `if (x > 0) y := 1;` → erro (falta `{`)

---

## H7 — Laços de repetição

**Descrição:** Como compilador, preciso reconhecer os comandos `do-while` e `repeat-until` para validar estruturas de repetição.

**Depende de:** H5

**Critérios de aceitação:**
- `doStmt()` reconhece `do "{" stmt-list "}" while "(" condition ")"`
- `repeatStmt()` reconhece `repeat "{" stmt-list "}" until "(" condition ")"`
- Ausência de `while` após `}` em `do` → `SyntacticException`
- Ausência de `until` após `}` em `repeat` → `SyntacticException`
- Ausência de `(` ou `)` em qualquer sufixo → `SyntacticException`

**Casos de teste TDD:**
- `do { x := x + 1; } while (x < 10);` → sucesso
- `repeat { x := x + 1; } until (x >= 10);` → sucesso
- `do { x := 1; } until (x > 0);` → erro (esperava `while`, não `until`)
- `repeat { x := 1; } while (x > 0);` → erro (esperava `until`, não `while`)
- `do { x := 1; } while x < 10;` → erro (falta `(`)

---

## H8 — Comandos read e write

**Descrição:** Como compilador, preciso reconhecer `read(id)` e `write(expr)` para validar entrada e saída.

**Depende de:** H4

**Critérios de aceitação:**
- `readStmt()` reconhece `read "(" identifier ")"`
- `writeStmt()` reconhece `write "(" writable ")"`
- `writable()` delega para `simpleExpr()`
- `read` sem `(` → `SyntacticException`
- `read` com expressão em vez de identificador → `SyntacticException`
- `write` sem `(` → `SyntacticException`

**Casos de teste TDD:**
- `read(x);` → sucesso
- `write("ola");` → sucesso
- `write(a + b);` → sucesso
- `write(3.14);` → sucesso
- `read(x + 1);` → erro (expressão em posição de identificador)
- `write ;` → erro (falta `(`)

---

## H9 — Integração: casos de teste do trabalho

**Descrição:** Como compilador, preciso validar os 6 programas-teste especificados no enunciado, reportando sucesso ou o erro encontrado com o número de linha correto.

**Depende de:** H2–H8 completos

**Critérios de aceitação:**
- Cada teste é executado e seu resultado (sucesso ou `SyntacticException`) é reportado
- Para programas com erro, a mensagem informa a linha exata
- Os programas corrigidos (sem erros léxicos ou sintáticos) são reconhecidos com sucesso

**Casos de teste TDD:**

> Os arquivos de teste estão em `src/resources/inputs/`. Os erros sintáticos esperados assumem que os erros léxicos já foram corrigidos.

| Arquivo    | Erros sintáticos esperados |
|------------|----------------------------|
| Teste1.txt | Falta `class` no início; falta `)` em `read(altura`; falta `;` após `area := ...` |
| Teste2.txt | Comentário de bloco não-fechado (erro léxico que impede análise sintática) |
| Teste3.txt | `if` sem `{`; `maior = a` usa `=` em vez de `:=` |
| Teste4.txt | Programa não começa com `class`; string não-fechada |
| Teste5.txt | `IF` maiúsculo (case-sensitive → ID, não palavra reservada); `:=` errado em declaração |
| Teste6.txt | Deve ser implementado como programa válido pelo aluno |

---

## Ordem de desenvolvimento recomendada

```
H1 → H2 → H3 → H4 → H5 → H6 → H7 → H8 → H9
```

Cada história pode ser desenvolvida, testada e integrada de forma independente antes de avançar para a próxima.
