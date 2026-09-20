package br.edu.transacoes;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main {

    private static void printLinha(char c, int n) {
        for (int i = 0; i < n; i++) System.out.print(c);
        System.out.println();
    }

    private static String[] quebrarChave(String chaveConta) {
        // TITULAR|BANCO|AGENCIA|CONTA
        String[] p = chaveConta.split("\\|", -1);
        if (p.length != 4) return new String[] { chaveConta, "?", "?", "?" };
        return p;
    }

    public static void main(String[] args) throws Exception {
        String caminho = (args.length > 0 && args[0] != null && !args[0].isBlank())
                ? args[0]
                : "dados/transacoes.csv";

        Path arquivo = Path.of(caminho);

        LeitorCSV leitor = new LeitorCSV();
        List<Transacao> validas = leitor.ler(arquivo);

        long ini = System.nanoTime();

        ProcessadorTransacoes proc = new ProcessadorTransacoes();
        List<Transacao> unicas = proc.removerDuplicatas(validas);
        Map<String, List<Transacao>> porConta = proc.agruparPorContaEOrdenar(unicas);
        Map<String, Double> saldos = proc.calcularSaldos(porConta);

        long ms = (System.nanoTime() - ini) / 1_000_000;

        // titulares distintos (para estatistica)
        Set<String> titulares = new HashSet<>();
        for (String k : porConta.keySet()) {
            titulares.add(quebrarChave(k)[0]);
        }

        printLinha('=', 72);
        System.out.println("PROCESSADOR DE TRANSACOES BANCARIAS");
        printLinha('=', 72);
        System.out.printf("Arquivo................: %s%n", caminho);
        System.out.printf("Linhas de dados lidas...: %d%n", leitor.getLinhasDadosLidas());
        System.out.printf("Linhas invalidas........: %d%n", leitor.getLinhasInvalidas());
        System.out.printf("Duplicatas removidas....: %d%n", proc.getDuplicatasRemovidas());
        System.out.printf("Transacoes validas......: %d%n", validas.size());
        System.out.printf("Transacoes apos dedup...: %d%n", unicas.size());
        System.out.printf("Titulares distintos.....: %d%n", titulares.size());
        System.out.printf("Contas distintas........: %d%n", porConta.size());
        System.out.printf("Tempo de processamento..: %d ms%n", ms);
        System.out.println();

        // Impressao organizada por TITULAR -> CONTAS -> TRANSACOES
        String titularAtual = null;

        for (Map.Entry<String, List<Transacao>> e : porConta.entrySet()) {
            String chave = e.getKey();
            String[] p = quebrarChave(chave);

            String titular = p[0];
            String banco = p[1];
            String agencia = p[2];
            String conta = p[3];

            if (!titular.equals(titularAtual)) {
                titularAtual = titular;
                printLinha('-', 72);
                System.out.printf("TITULAR: %s%n", titularAtual);
                printLinha('-', 72);
            }

            System.out.printf("BANCO: %s   AGENCIA: %s   CONTA: %s%n", banco, agencia, conta);
            System.out.println("--------------------------------------------------------------------");

            for (Transacao t : e.getValue()) {
                System.out.println("  " + t);
            }

            Double saldo = saldos.get(chave);
            if (saldo == null) saldo = 0.0;

            System.out.printf("  >> SALDO FINAL: R$ %.2f%n%n", saldo);
        }
    }
}