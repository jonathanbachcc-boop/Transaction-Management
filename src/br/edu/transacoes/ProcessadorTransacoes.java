package br.edu.transacoes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/*
 * Core do projeto:
 * - remove duplicatas exatas
 * - agrupa por conta (chave: TITULAR|BANCO|AGENCIA|CONTA)
 * - ordena por data/hora
 * - calcula saldo final por conta (saldo inicial = 0)
 *
 * Obs: o requisito "organizadas por titular" pode ser atendido na impressao
 * do relatorio (Main), imprimindo TITULAR -> contas -> transacoes.
 */
public class ProcessadorTransacoes {

    /*
     * Comparator principal:
     * - data/hora crescente
     * - empates: tipo e valor (deterministico)
     *
     * A estabilidade real depende do MergeSort ser estavel.
     */
    public static final Comparator<Transacao> POR_DATA_HORA =
            Comparator.comparing(Transacao::getDataHora)
                    .thenComparing(t -> t.getTipo().name())
                    .thenComparingDouble(Transacao::getValor);

    private int duplicatasRemovidas = 0;

    public int getDuplicatasRemovidas() {
        return duplicatasRemovidas;
    }

    /*
     * Remove duplicatas exatas preservando a ordem de leitura.
     * Complexidade media: O(n).
     */
    public List<Transacao> removerDuplicatas(List<Transacao> entrada) {
        duplicatasRemovidas = 0;

        int n = entrada.size();
        int cap = (int) (n / 0.75f) + 1; // capacidade para evitar rehash

        Set<Transacao> vistas = new HashSet<>(Math.max(16, cap));
        List<Transacao> unicas = new ArrayList<>(n);

        for (Transacao t : entrada) {
            if (vistas.add(t)) {
                unicas.add(t);
            } else {
                duplicatasRemovidas++;
            }
        }
        return unicas;
    }

    /*
     * Agrupa por conta e ordena as transacoes de cada conta por data/hora.
     * Retorna TreeMap para manter saida em ordem alfabetica da chave.
     *
     * Complexidade:
     * - agrupamento: O(n log k) (k = num de contas)
     * - ordenacao total: O(n log n) (pior caso)
     */
    public Map<String, List<Transacao>> agruparPorContaEOrdenar(List<Transacao> transacoes) {
        Map<String, List<Transacao>> porConta = new TreeMap<>();

        for (Transacao t : transacoes) {
            String chave = t.chaveConta();
            porConta.computeIfAbsent(chave, k -> new ArrayList<>()).add(t);
        }

        for (List<Transacao> grupo : porConta.values()) {
            MergeSort.ordenar(grupo, POR_DATA_HORA);
        }

        return porConta;
    }

    /*
     * Calcula saldo final por conta assumindo saldo inicial 0.
     * Complexidade: O(n).
     */
    public Map<String, Double> calcularSaldos(Map<String, List<Transacao>> porConta) {
        Map<String, Double> saldos = new LinkedHashMap<>();

        for (Map.Entry<String, List<Transacao>> e : porConta.entrySet()) {
            double saldo = 0.0;
            for (Transacao t : e.getValue()) {
                saldo += t.impactoNoSaldo();
            }
            saldos.put(e.getKey(), saldo);
        }

        return saldos;
    }
}