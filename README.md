# Processador de Transações Bancárias — Estrutura de Dados I

Aplicação Java de console que lê um CSV com muitas transações bancárias
desordenadas, remove duplicatas, agrupa por titular/conta, ordena por data e
hora e mostra o saldo final de cada conta (todas iniciam zeradas).

## Como rodar

```bash
./executar.sh                       # usa dados/transacoes.csv
./executar.sh caminho/do/arquivo.csv
```

Ou manualmente:

```bash
mkdir -p out
javac -d out $(find src -name "*.java")
java -cp out br.edu.transacoes.Main dados/transacoes.csv
```

Requer apenas JDK 17+ (sem dependências externas).

## Layout do CSV

```
AGENCIA,CONTA,BANCO,TITULAR,OPERACAO,DATAHORA,VALOR
1520,0001,SANTANDER,JOAO,SAQUE,2022-02-10T10:13:39,250.00
```

A coluna `VALOR` foi adicionada ao layout original, como o enunciado permite.
`OPERACAO` aceita `SAQUE` ou `DEPOSITO`. Linhas com data malformada, valor não
numérico, valor <= 0 ou operação desconhecida são descartadas e contabilizadas.

## Estruturas de dados escolhidas (e por quê)

| Etapa | Estrutura | Por quê | Big O |
|---|---|---|---|
| Leitura | `ArrayList<Transacao>` | inserção amortizada O(1) e acesso por índice O(1), necessário para o merge sort | O(n) |
| Remoção de duplicatas | `HashSet<Transacao>` | `equals`/`hashCode` da `Transacao` codificam a regra de duplicata; teste de pertinência em O(1) médio | O(n) médio |
| Agrupamento por titular | `TreeMap<String, List<Transacao>>` | agrupa e já entrega os titulares em ordem alfabética na saída | O(n log k) |
| Ordenação por data/hora | `MergeSort` próprio + `Comparator` | estável e O(n log n) garantido no pior caso | O(n log n) |
| Saldos | `LinkedHashMap<String, Double>` | preserva a ordem dos grupos, acesso O(1) | O(n) |

Total do pipeline: **O(n log n)**, dominado pela ordenação.

## Regra de duplicata

Duas transações são duplicatas quando têm **exatamente** o mesmo valor, o mesmo
tipo (saque/depósito), o mesmo operador (titular + banco + agência + conta) e a
mesma data/hora. Implementada em `Transacao.equals()`/`hashCode()`, o que
permite ao `HashSet` detectar duplicatas em tempo constante médio — em vez de
comparar todos os pares, que seria O(n²).

## Análise do Merge Sort (para o quadro branco)

```text
                 [n]                     nivel 0: 1 merge de n     -> O(n)
          [n/2]      [n/2]               nivel 1: 2 merges de n/2  -> O(n)
       [n/4][n/4]  [n/4][n/4]            nivel 2: 4 merges de n/4  -> O(n)
              ...                         ...
            [1][1][1]...                 nivel log2(n)
```

- Altura da recursão: `log2(n)` (a lista é dividida ao meio até sobrar 1).
- Cada nível intercala todos os `n` elementos: `O(n)`.
- Custo total: `O(n) * log n = O(n log n)` — melhor, médio e pior caso.
- Espaço: `O(n)` pelo vetor auxiliar; algoritmo **estável**.
- Recorrência: `T(n) = 2T(n/2) + O(n)` → pelo Teorema Mestre, `Θ(n log n)`.

Comparação: inserção/seleção seriam `O(n²)`; quick sort é `O(n log n)` médio mas
`O(n²)` no pior caso e não é estável. Para transações com data/hora repetida, a
estabilidade preserva a ordem de leitura — por isso merge sort.

## Estrutura dos arquivos

```
src/br/edu/transacoes/
  Transacao.java              modelo + regra de duplicata (equals/hashCode)
  LeitorCSV.java              parsing e validação do arquivo
  MergeSort.java              ordenação O(n log n) implementada à mão
  ProcessadorTransacoes.java  duplicatas, agrupamento, ordenação, saldos
  Main.java                   relatório no console
dados/transacoes.csv          amostra com duplicatas e linhas inválidas
executar.sh                   compila e roda
```

## Sugestões para o live coding

Alterações prováveis e onde mexer:
- ordenar por valor em vez de data → trocar o `Comparator` em `ProcessadorTransacoes.POR_DATA_HORA`;
- ordem decrescente → `.reversed()` no comparador;
- agrupar só por titular → simplificar `Transacao.chaveConta()`;
- ignorar duplicatas dentro de 1 segundo → ajustar `equals`/`hashCode` ou filtrar após ordenar;
- bloquear saques sem saldo → validar em `ProcessadorTransacoes.calcularSaldos()`.
