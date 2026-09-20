# Comentários do Código — Processador de Transações Bancárias

Explicação método por método, da função e da lógica de cada trecho de código.
Ideal para revisar antes da apresentação e do live coding.

---

## 1. `Transacao.java` — o modelo de dados

Classe que representa **uma linha do CSV** como objeto Java. É `final`
(imutável): depois de criada, nenhum campo pode ser alterado, o que evita
erros de concorrência e garante que `equals`/`hashCode` fiquem estáveis.

### `enum Tipo { SAQUE, DEPOSITO }`
Enum com as duas operações possíveis. Usar enum (em vez de `String`) dá
segurança de tipo: um valor inválido falha na leitura, não no meio do
processamento.

### Construtor `Transacao(...)`
Recebe os 7 campos (agência, conta, banco, titular, tipo, dataHora, valor)
e apenas os armazena. Como a classe é imutável, não existem setters.

### Getters (`getAgencia()`, `getConta()`, ...)
Acesso de leitura a cada campo. São usados pelo comparador de ordenação
(`Transacao::getDataHora`, `Transacao::getValor`) e pelo leitor/processor.

### `chaveConta()`
**Função:** devolve uma `String` única por conta, no formato
`TITULAR|BANCO|AGENCIA|CONTA`.
**Lógica:** o agrupamento é feito por titular **e** conta. O separador `|`
evita colisões (ex.: titular "AB" + banco "C" ≠ titular "A" + banco "BC").
É a chave usada no `TreeMap` do processador.

### `impactoNoSaldo()`
**Função:** devolve o valor com sinal para o cálculo do saldo.
**Lógica:** `DEPOSITO` retorna `+valor`, `SAQUE` retorna `-valor`. Assim o
saldo é uma simples soma: `saldo += t.impactoNoSaldo()`.

### `equals(Object o)`
**Função:** define quando duas transações são **duplicatas**.
**Lógica (regra do enunciado):** duplicata = mesmo **valor** + mesmo
**tipo** + mesma **data/hora exata** + mesmo **operador** (agência, conta,
banco, titular). Compara `valor` com `Double.compare` (comparação correta
para ponto flutuante) e os demais campos com `Objects.equals` (seguro
contra `null`).

### `hashCode()`
**Função:** compatibiliza a classe com `HashSet`/`HashMap`.
**Lógica:** gera o hash a partir dos **mesmos campos do `equals`** —
contrato obrigatório: objetos iguais precisam ter o mesmo hash. Sem isso o
`HashSet` não detectaria as duplicatas.

### `toString()`
**Função:** formata a transação para exibição no relatório
(`20/01/2026 10:00:00 | ag 0001 cc 12345-6 | DEPOSITO | R$ 1.500,00`).
**Lógica:** usa `DateTimeFormatter` no padrão brasileiro e `String.format`
para alinhar o valor monetário.

---

## 2. `LeitorCSV.java` — leitura e validação do arquivo

Responsável por transformar o texto do CSV em objetos `Transacao`,
descartando linhas inválidas.

### `ler(Path arquivo)`
**Função:** lê o CSV inteiro e devolve a lista de transações válidas.
**Lógica:**
- `ArrayList` é escolhido porque a inserção é O(1) amortizada e o acesso
  por índice é O(1) — ideal para o merge sort que vem depois.
- `try-with-resources` garante o fechamento do arquivo mesmo em caso de erro.
- A **primeira linha (cabeçalho) é lida e ignorada**.
- Linhas em branco são puladas; as demais passam por `converter()`. Se a
  conversão falhar (`null`), o contador de inválidas aumenta.
- **Complexidade:** O(n) — cada linha é processada uma única vez.

### `converter(String linha)` (privado)
**Função:** transforma uma linha de texto em `Transacao`, ou devolve `null`
se a linha for inválida.
**Lógica:**
1. `split(",", -1)` quebra nos campos (o `-1` preserva campos vazios no fim);
   menos de 7 campos → inválida.
2. Normaliza: `trim()` em todos, `toUpperCase()` em banco, titular e tipo —
   evita que "bb" e "BB" virem contas diferentes.
3. Converte tipos: `Tipo.valueOf` (falha se não for SAQUE/DEPOSITO),
   `LocalDateTime.parse` (falha se a data estiver malformada) e
   `Double.parseDouble` (falha se o valor não for número).
4. **Regra de validação:** valor ≤ 0 é rejeitado.
5. Qualquer exceção (`IllegalArgumentException`, `DateTimeParseException`)
   é capturada e a linha é descartada — um dado ruim não derruba o programa.

### `getLinhasInvalidas()`
Devolve quantas linhas foram descartadas — usado no relatório final.

---

## 3. `ProcessadorTransacoes.java` — o núcleo

Faz as três etapas principais: remover duplicatas, agrupar/ordenar e
calcular saldos.

### Constante `POR_DATA_HORA`
**Função:** comparador que define a ordem das transações dentro de cada conta.
**Lógica:** ordena por **data/hora crescente**; em empate, desempata por
tipo e depois por valor, garantindo saída sempre idêntica (determinística).

### `removerDuplicatas(List<Transacao> entrada)`
**Função:** elimina duplicatas exatas, preservando a ordem de leitura.
**Lógica:** percorre a lista uma vez. `HashSet.add(t)` devolve `true` se o
elemento **não** estava no conjunto — então é adicionado à saída; se
devolver `false`, é duplicata e só incrementa o contador.
**Por que `HashSet`:** consulta/inserção em **O(1) médio** graças ao
`hashCode()` da `Transacao` — total **O(n)** em vez do O(n²) de comparar
todos com todos. O construtor com capacidade inicial evita redimensionamentos.

### `agruparEOrdenar(List<Transacao> transacoes)`
**Função:** separa as transações por conta e ordena cada grupo por data/hora.
**Lógica:**
1. `TreeMap` mantém as chaves (contas) **automaticamente ordenadas
   alfabeticamente** — inserção em O(log k).
2. `computeIfAbsent(chave, k -> new ArrayList<>())`: cria a lista da conta
   na primeira vez que ela aparece; depois só dá `add`.
3. Para cada grupo, chama `MergeSort.ordenar` com o comparador
   `POR_DATA_HORA` — O(n log n) por grupo.
**Total:** O(n log k) + O(n log n).

### `calcularSaldos(Map<String, List<Transacao>> porConta)`
**Função:** calcula o saldo final de cada conta (todas começam zeradas).
**Lógica:** para cada conta, soma `impactoNoSaldo()` de cada transação
(depósito soma, saque subtrai). Usa `LinkedHashMap` para manter a mesma
ordem de exibição do mapa de contas. **Complexidade:** O(n).

### `getDuplicatasRemovidas()`
Devolve o total de duplicatas encontradas — exibido no relatório.

---

## 4. `MergeSort.java` — ordenação manual (para o quadro)

Implementação própria do merge sort, exigida para a análise de complexidade.

### `ordenar(List<T> lista, Comparator comparator)`
**Função:** ponto de entrada — ordena qualquer lista com qualquer comparador
(método genérico `<T>`).
**Lógica:** lista com menos de 2 elementos já está ordenada (caso base).
Copia para um array (acesso O(1) por índice), cria o **array auxiliar**
`aux` (daí o custo espacial O(n)), chama a recursão e copia o resultado de
volta para a lista.

### `dividir(a, aux, inicio, fim, cmp)` (privado, recursivo)
**Função:** fase de **divisão** do algoritmo.
**Lógica:** se o trecho tem 0 ou 1 elemento, retorna (caso base). Senão:
calcula o meio com `inicio + (fim - inicio) / 2` (forma que evita overflow),
chama a si mesmo para a **metade esquerda** e para a **metade direita**, e
por fim intercala as duas metades já ordenadas. A lista é dividida ao meio
`log₂(n)` vezes — essa é a altura da árvore de recursão.

### `intercalar(a, aux, inicio, meio, fim, cmp)` (privado)
**Função:** fase de **conquista** — funde duas metades ordenadas em uma só.
**Lógica:**
1. Copia o trecho para o array auxiliar.
2. Dois ponteiros: `e` percorre a metade esquerda, `d` a direita.
3. Para cada posição, compara as cabeças das duas metades:
   - esquerda esgotada → copia da direita;
   - direita esgotada → copia da esquerda;
   - `aux[d] < aux[e]` → copia da direita;
   - caso contrário → copia da esquerda (o `<=` **mantém a estabilidade**:
     elementos iguais ficam na ordem original).

**Análise Big O (para o quadro):**
- Altura da recursão: `log₂ n` níveis.
- Cada nível intercala todos os `n` elementos → O(n) por nível.
- **Tempo: O(n log n) no melhor, médio e pior caso.**
- **Espaço: O(n)** pelo array auxiliar. Algoritmo **estável**.

---

## 5. `Main.java` — ponto de entrada

Orquestra tudo e imprime o relatório.

### `main(String[] args)`
**Lógica, passo a passo:**
1. **Argumento:** usa o caminho passado na linha de comando; se nenhum,
   assume `dados/transacoes.csv`.
2. **Leitura:** chama `LeitorCSV.ler()`. Se o arquivo não existir ou não
   puder ser lido, `IOException` é capturada, uma mensagem amigável vai
   para `System.err` e o programa encerra sem stack trace.
3. **Cronometragem:** `System.nanoTime()` antes e depois do processamento
   para medir o tempo em milissegundos.
4. **Pipeline:** `removerDuplicatas` → `agruparEOrdenar` → `calcularSaldos`.
5. **Resumo:** imprime estatísticas (lidas, inválidas, duplicatas, válidas,
   contas, tempo).
6. **Relatório por conta:** percorre o mapa ordenado; a chave
   `TITULAR|BANCO|AGENCIA|CONTA` é quebrada com `split("\\|")` para o
   cabeçalho; cada transação usa o `toString()` da `Transacao`; ao final de
   cada grupo imprime o saldo com duas casas (`%.2f`).

---

## Resumo das estruturas e complexidades

| Etapa | Estrutura | Por quê | Complexidade |
|---|---|---|---|
| Leitura | `ArrayList` | inserção O(1) amortizada, acesso O(1) | O(n) |
| Duplicatas | `HashSet` | detecção O(1) média via `hashCode` | O(n) médio |
| Agrupamento | `TreeMap` | contas já saem ordenadas | O(n log k) |
| Ordenação | Merge sort próprio | O(n log n) garantido, estável | O(n log n) |
| Saldos | `LinkedHashMap` | soma simples, ordem preservada | O(n) |
