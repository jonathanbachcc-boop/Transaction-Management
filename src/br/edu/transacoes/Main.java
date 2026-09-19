package br.edu.transacoes;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Ponto de entrada.
 *
 * Uso: java -cp out br.edu.transacoes.Main dados/transacoes.csv
 */
public class Main {

    public static void main(String[] args) {
        String caminho = args.length > 0 ? args[0] : "dados/transacoes.csv";

        LeitorCSV leitor = new LeitorCSV();
        List<Transacao> brutas;
        try {
            brutas = leitor.ler(Path.of(caminho));
        } catch (IOException e) {
            System.err.println("Nao foi possivel ler o arquivo: " + caminho);
            System.err.println("Motivo: " + e.getMessage());
            return;
        }

        long inicio = System.nanoTime();

        ProcessadorTransacoes processador = new ProcessadorTransacoes();
        List<Transacao> unicas = processador.removerDuplicatas(brutas);
        Map<String, List<Transacao>> porConta = processador.agruparEOrdenar(unicas);
        Map<String, Double> saldos = processador.calcularSaldos(porConta);

        long ms = (System.nanoTime() - inicio) / 1_000_000;

        System.out.println("=".repeat(72));
        System.out.println("PROCESSADOR DE TRANSACOES BANCARIAS");
        System.out.println("=".repeat(72));
        System.out.printf("Arquivo................: %s%n", caminho);
        System.out.printf("Transacoes lidas.......: %d%n", brutas.size());
        System.out.printf("Linhas invalidas.......: %d%n", leitor.getLinhasInvalidas());
        System.out.printf("Duplicatas removidas...: %d%n", processador.getDuplicatasRemovidas());
        System.out.printf("Transacoes validas.....: %d%n", unicas.size());
        System.out.printf("Contas distintas.......: %d%n", porConta.size());
        System.out.printf("Tempo de processamento.: %d ms%n", ms);

        for (Map.Entry<String, List<Transacao>> entry : porConta.entrySet()) {
            String[] p = entry.getKey().split("\\|");
            System.out.println();
            System.out.println("-".repeat(72));
            System.out.printf("TITULAR: %s   BANCO: %s   AGENCIA: %s   CONTA: %s%n",
                    p[0], p[1], p[2], p[3]);
            System.out.println("-".repeat(72));
            for (Transacao t : entry.getValue()) {
                System.out.println("  " + t);
            }
            System.out.printf("  >> SALDO FINAL: R$ %.2f%n", saldos.get(entry.getKey()));
        }
        System.out.println();
    }
}
