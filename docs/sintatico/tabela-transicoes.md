# Tabela de Transições — Analisador Sintático

## Gramática de referência

```
P1.  program     ::= class identifier "{" [decl-list] body "}"
P2.  decl-list   ::= decl ";" { decl ";" }
P3.  decl        ::= type ident-list
P4.  ident-list  ::= identifier { "," identifier }
P5.  type        ::= int | string | float
P6.  body        ::= "{" stmt-list "}"
P7.  stmt-list   ::= stmt ";" { stmt ";" }
P8.  stmt        ::= assign-stmt | if-stmt | do-stmt | repeat-stmt | read-stmt | write-stmt
P9.  assign-stmt ::= identifier ":=" simple-expr
P10. if-stmt     ::= if "(" condition ")" "{" stmt-list "}" if-stmt'
P11. if-stmt'    ::= else "{" stmt-list "}" | λ
P12. do-stmt     ::= do "{" stmt-list "}" do-suffix
P13. do-suffix   ::= while "(" condition ")"
P14. repeat-stmt ::= repeat "{" stmt-list "}" stmt-suffix
P15. stmt-suffix ::= until "(" condition ")"
P16. read-stmt   ::= read "(" identifier ")"
P17. write-stmt  ::= write "(" writable ")"
P18. writable    ::= simple-expr
P19. condition   ::= expression
P20. expression  ::= simple-expr expression'
P21. expression' ::= relop simple-expr | λ
P22. simple-expr ::= term simple-expr'
P23. simple-expr'::= addop term simple-expr' | λ
P24. term        ::= factor-a term'
P25. term'       ::= mulop factor-a term' | λ
P26. factor-a    ::= factor | not factor | "-" factor
P27. factor      ::= identifier | constant | "(" expression ")"
P28. relop       ::= ">" | ">=" | "<" | "<=" | "<>" | "="
P29. addop       ::= "+" | "-" | or
P30. mulop       ::= "*" | "/" | "%" | and
```

## Convenções da tabela

| Símbolo | Significado |
|---------|-------------|
| **Pn**  | Aplicar a produção n (alternativa não-vazia) |
| **λ**   | Aplicar alternativa vazia (não consome token) |
| *(vazio)* | Célula de erro — token inválido neste contexto |

> **Nota sobre colunas duplicadas:** `=` é o operador relacional de igualdade (relop). `:=` é o operador de atribuição (assign), e não aparece em posições de expressão.

---

## Tabela

### Parte 1 — Palavras reservadas e identificadores

| Não-terminal | `class` | `int` | `float` | `string` | `if` | `else` | `do` | `while` | `repeat` | `until` | `read` | `write` | `not` | `and` | `or` | `id` | `cte` |
|---|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| program      | **P1**  |       |       |        |       |       |       |       |        |       |       |        |       |       |      |       |       |
| decl-list    |         | **P2**| **P2**| **P2** |       |       |       |       |        |       |       |        |       |       |      |       |       |
| decl         |         | **P3**| **P3**| **P3** |       |       |       |       |        |       |       |        |       |       |      |       |       |
| ident-list   |         |       |       |        |       |       |       |       |        |       |       |        |       |       |      | **P4**|       |
| type         |         | **P5**| **P5**| **P5** |       |       |       |       |        |       |       |        |       |       |      |       |       |
| body         |         |       |       |        |       |       |       |       |        |       |       |        |       |       |      |       |       |
| stmt-list    |         |       |       |        | **P7**|       | **P7**|       | **P7** |       | **P7**| **P7** |       |       |      | **P7**|       |
| stmt         |         |       |       |        | **P8**|       | **P8**|       | **P8** |       | **P8**| **P8** |       |       |      | **P8**|       |
| assign-stmt  |         |       |       |        |       |       |       |       |        |       |       |        |       |       |      | **P9**|       |
| if-stmt      |         |       |       |        |**P10**|       |       |       |        |       |       |        |       |       |      |       |       |
| if-stmt'     |         |       |       |        |       |**P11**|       |       |        |       |       |        |       |       |      |       |       |
| do-stmt      |         |       |       |        |       |       |**P12**|       |        |       |       |        |       |       |      |       |       |
| do-suffix    |         |       |       |        |       |       |       |**P13**|        |       |       |        |       |       |      |       |       |
| repeat-stmt  |         |       |       |        |       |       |       |       | **P14**|       |       |        |       |       |      |       |       |
| stmt-suffix  |         |       |       |        |       |       |       |       |        |**P15**|       |        |       |       |      |       |       |
| read-stmt    |         |       |       |        |       |       |       |       |        |       |**P16**|        |       |       |      |       |       |
| write-stmt   |         |       |       |        |       |       |       |       |        |       |       | **P17**|       |       |      |       |       |
| writable     |         |       |       |        |       |       |       |       |        |       |       |        |**P18**|       |      |**P18**|**P18**|
| condition    |         |       |       |        |       |       |       |       |        |       |       |        |**P19**|       |      |**P19**|**P19**|
| expression   |         |       |       |        |       |       |       |       |        |       |       |        |**P20**|       |      |**P20**|**P20**|
| expression'  |         |       |       |        |       |       |       |       |        |       |       |        |       |       |      |       |       |
| simple-expr  |         |       |       |        |       |       |       |       |        |       |       |        |**P22**|       |      |**P22**|**P22**|
| simple-expr' |         |       |       |        |       |       |       |       |        |       |       |        |       |**P23**|**P23**|      |       |
| term         |         |       |       |        |       |       |       |       |        |       |       |        |**P24**|       |      |**P24**|**P24**|
| term'        |         |       |       |        |       |       |       |       |        |       |       |        |       |**P25**| **λ**|      |       |
| factor-a     |         |       |       |        |       |       |       |       |        |       |       |        |**P26**|       |      |**P26**|**P26**|
| factor       |         |       |       |        |       |       |       |       |        |       |       |        |       |       |      |**P27**|**P27**|
| relop        |         |       |       |        |       |       |       |       |        |       |       |        |       |       |      |       |       |
| addop        |         |       |       |        |       |       |       |       |        |       |       |        |       |       |**P29**|      |       |
| mulop        |         |       |       |        |       |       |       |       |        |       |       |        |       |**P30**|       |      |       |

### Parte 2 — Pontuação e operadores

| Não-terminal | `{` | `}` | `(` | `)` | `;` | `,` | `:=` | `=` | `+` | `-` | `*` | `/` | `%` | `>` | `>=` | `<` | `<=` | `<>` |
|---|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| program      |     |     |     |     |     |     |     |     |     |     |     |     |     |     |      |     |      |      |
| decl-list    |     |     |     |     |     |     |     |     |     |     |     |     |     |     |      |     |      |      |
| decl         |     |     |     |     |     |     |     |     |     |     |     |     |     |     |      |     |      |      |
| ident-list   |     |     |     |     |     |     |     |     |     |     |     |     |     |     |      |     |      |      |
| type         |     |     |     |     |     |     |     |     |     |     |     |     |     |     |      |     |      |      |
| body         |**P6**|    |     |     |     |     |     |     |     |     |     |     |     |     |      |     |      |      |
| stmt-list    |     |     |     |     |     |     |     |     |     |     |     |     |     |     |      |     |      |      |
| stmt         |     |     |     |     |     |     |     |     |     |     |     |     |     |     |      |     |      |      |
| assign-stmt  |     |     |     |     |     |     |     |     |     |     |     |     |     |     |      |     |      |      |
| if-stmt      |     |     |     |     |     |     |     |     |     |     |     |     |     |     |      |     |      |      |
| if-stmt'     |     |     |     |     | **λ**|    |     |     |     |     |     |     |     |     |      |     |      |      |
| do-stmt      |     |     |     |     |     |     |     |     |     |     |     |     |     |     |      |     |      |      |
| do-suffix    |     |     |     |     |     |     |     |     |     |     |     |     |     |     |      |     |      |      |
| repeat-stmt  |     |     |     |     |     |     |     |     |     |     |     |     |     |     |      |     |      |      |
| stmt-suffix  |     |     |     |     |     |     |     |     |     |     |     |     |     |     |      |     |      |      |
| read-stmt    |     |     |     |     |     |     |     |     |     |     |     |     |     |     |      |     |      |      |
| write-stmt   |     |     |     |     |     |     |     |     |     |     |     |     |     |     |      |     |      |      |
| writable     |     |     |**P18**|    |    |     |     |     |     |**P18**|   |     |     |     |      |     |      |      |
| condition    |     |     |**P19**|    |    |     |     |     |     |**P19**|   |     |     |     |      |     |      |      |
| expression   |     |     |**P20**|    |    |     |     |     |     |**P20**|   |     |     |     |      |     |      |      |
| expression'  |     |     |     | **λ**|   |     |     |**P21**|   |    |     |     |     |**P21**|**P21**|**P21**|**P21**|**P21**|
| simple-expr  |     |     |**P22**|   |    |     |     |     |     |**P22**|   |     |     |     |      |     |      |      |
| simple-expr' |     |     |     | **λ**|**λ**|   |     |     |**P23**|**P23**| |     |     | **λ**| **λ**| **λ**| **λ**| **λ**|
| term         |     |     |**P24**|   |    |     |     |     |     |**P24**|   |     |     |     |      |     |      |      |
| term'        |     |     |     | **λ**|**λ**|   |     |     | **λ**| **λ**|**P25**|**P25**|**P25**|**λ**|**λ**|**λ**|**λ**|**λ**|
| factor-a     |     |     |**P26**|   |    |     |     |     |     |**P26**|   |     |     |     |      |     |      |      |
| factor       |     |     |**P27**|   |    |     |     |     |     |     |     |     |     |     |      |     |      |      |
| relop        |     |     |     |     |     |     |     |**P28**|   |    |     |     |     |**P28**|**P28**|**P28**|**P28**|**P28**|
| addop        |     |     |     |     |     |     |     |     |**P29**|**P29**| |     |     |     |      |     |      |      |
| mulop        |     |     |     |     |     |     |     |     |     |    |**P30**|**P30**|**P30**|    |      |     |      |      |

---

## FIRST e FOLLOW relevantes

| Conjunto | Tokens |
|---|---|
| FIRST(type) | `int`, `float`, `string` |
| FIRST(stmt) | `id`, `if`, `do`, `repeat`, `read`, `write` |
| FIRST(simple-expr) | `id`, `cte`, `(`, `not`, `-` |
| FIRST(relop) | `>`, `>=`, `<`, `<=`, `<>`, `=` |
| FIRST(addop) | `+`, `-`, `or` |
| FIRST(mulop) | `*`, `/`, `%`, `and` |
| FOLLOW(if-stmt') | `;` |
| FOLLOW(expression') | `)` |
| FOLLOW(simple-expr') | `)`, `;`, `>`, `>=`, `<`, `<=`, `<>`, `=` |
| FOLLOW(term') | FOLLOW(simple-expr') ∪ FIRST(addop) |
