package br.edu.transacoes;

import java.util.Comparator;
import java.util.List;

/**
 * Merge sort implementado manualmente (para a analise no quadro branco).
 *
 * Complexidade temporal: O(n log n) no melhor, medio e pior caso.
 *   - a lista e dividida ao meio log2(n) vezes  -> altura da recursao = log n
 *   - cada nivel faz o merge de todos os n elementos -> O(n) por nivel
 *   => O(n log n)
 * Complexidade espacial: O(n) (vetor auxiliar) — algoritmo ESTAVEL.
 */
public final class MergeSort {

    private MergeSort() { }

    public static <T> void ordenar(List<T> lista, Comparator<? super T> comparator) {
        if (lista.size() < 2) return;
        @SuppressWarnings("unchecked")
        T[] itens = (T[]) lista.toArray();
        @SuppressWarnings("unchecked")
        T[] aux = (T[]) new Object[itens.length];
        dividir(itens, aux, 0, itens.length - 1, comparator);
        for (int i = 0; i < itens.length; i++) {
            lista.set(i, itens[i]);
        }
    }

    private static <T> void dividir(T[] a, T[] aux, int inicio, int fim,
                                    Comparator<? super T> cmp) {
        if (inicio >= fim) return;
        int meio = inicio + (fim - inicio) / 2;
        dividir(a, aux, inicio, meio, cmp);
        dividir(a, aux, meio + 1, fim, cmp);
        intercalar(a, aux, inicio, meio, fim, cmp);
    }

    private static <T> void intercalar(T[] a, T[] aux, int inicio, int meio, int fim,
                                       Comparator<? super T> cmp) {
        for (int i = inicio; i <= fim; i++) aux[i] = a[i];

        int e = inicio, d = meio + 1;
        for (int i = inicio; i <= fim; i++) {
            if (e > meio) {
                a[i] = aux[d++];
            } else if (d > fim) {
                a[i] = aux[e++];
            } else if (cmp.compare(aux[d], aux[e]) < 0) {
                a[i] = aux[d++];
            } else {
                a[i] = aux[e++]; // <= mantem a estabilidade
            }
        }
    }
}
