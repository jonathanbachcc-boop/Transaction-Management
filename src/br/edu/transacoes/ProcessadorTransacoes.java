package br.edu.transacoes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Nucleo do projeto: remove duplicatas, agrupa por titular/conta,
 * ordena por data e hora e calcula o saldo final.
 *
 * Estruturas escolhidas:
 *  - HashSet<Transacao>  -> deteccao de duplicata em O(1) medio (equals/hashCode da Transacao)
 *  - TreeMap<String, List<Transacao>> -> agrupamento por conta ja ordenado alfabeticamente, O(log k)
 *  - ArrayList<Transacao> -> lista por conta, ordenada com MergeSort O(n log n)
 */
public class ProcessadorTransacoes {

    /** Comparador principal: data/hora crescente; empates por tipo e valor (saida estavel). */
    public static final Comparator<Transacao> POR_DATA_HORA =
            Comparator.comparing(Transacao::getDataHora)
                      .thenComparing(t -> t.getTipo().name())
                      .thenComparingDouble(Transacao::getValor);

    private int duplicatasRemovidas = 0;

    /**
     * Remove duplicatas exatas preservando a ordem de leitura.
     * Complexidade: O(n) medio.
     */
    public List<Transacao> removerDuplicatas(List<Transacao> entrada) {
        Set<Transacao> vistas = new HashSet<>(Math.max(16, entrada.size() * 2));
        List<Transacao> unicas = new ArrayList<>(entrada.size());
        for (Transacao t : entrada) {
            if (vistas.add(t)) {
                unicas.add(t);
            } else {
                duplicatasRemovidas++;
            }
        }
        return unicas;
    }

    /**
     * Agrupa por titular + conta e ordena cada grupo por data/hora.
     * Complexidade: O(n log k) para agrupar + O(n log n) para ordenar.
     */
    public Map<String, List<Transacao>> agruparEOrdenar(List<Transacao> transacoes) {
        Map<String, List<Transacao>> porConta = new TreeMap<>();
        for (Transacao t : transacoes) {
            porConta.computeIfAbsent(t.chaveConta(), k -> new ArrayList<>()).add(t);
        }
        for (List<Transacao> grupo : porConta.values()) {
            MergeSort.ordenar(grupo, POR_DATA_HORA);
        }
        return porConta;
    }

    /**
     * Saldo final de cada conta (todas iniciam zeradas).
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

    public int getDuplicatasRemovidas() {
        return duplicatasRemovidas;
    }
}
