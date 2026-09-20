package br.edu.transacoes;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
 * Le CSV e valida linhas.
 *
 * Contadores:
 * - linhasDadosLidas: linhas de dados (exclui cabecalho), validas + invalidas
 * - linhasInvalidas: linhas descartadas por erro de formato/validacao
 *
 * Formato aceito:
 * AGENCIA,CONTA,BANCO,TITULAR,OPERACAO,DATAHORA,VALOR
 * (VALOR e obrigatorio para calculo de saldo)
 */
public class LeitorCSV {

    private int linhasDadosLidas = 0;
    private int linhasInvalidas = 0;

    public int getLinhasDadosLidas() {
        return linhasDadosLidas;
    }

    public int getLinhasInvalidas() {
        return linhasInvalidas;
    }

    public List<Transacao> ler(Path arquivo) throws IOException {
        linhasDadosLidas = 0;
        linhasInvalidas = 0;

        List<Transacao> transacoes = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(arquivo)) {
            String linha = br.readLine(); // cabecalho
            if (linha == null) return transacoes;

            while ((linha = br.readLine()) != null) {
                linhasDadosLidas++;

                String raw = linha.trim();
                if (raw.isEmpty()) {
                    linhasInvalidas++;
                    continue;
                }

                String[] c = raw.split(",", -1);
                // Esperado: 7 colunas (com VALOR)
                if (c.length != 7) {
                    linhasInvalidas++;
                    continue;
                }

                try {
                    String agencia = c[0].trim();
                    String conta = c[1].trim();
                    String banco = c[2].trim();
                    String titular = c[3].trim();
                    String operacao = c[4].trim();
                    String datahora = c[5].trim();
                    String valorStr = c[6].trim();

                    if (agencia.isEmpty() || conta.isEmpty() || banco.isEmpty() || titular.isEmpty()
                            || operacao.isEmpty() || datahora.isEmpty() || valorStr.isEmpty()) {
                        linhasInvalidas++;
                        continue;
                    }

                    Transacao.Tipo tipo;
                    if ("SAQUE".equalsIgnoreCase(operacao)) tipo = Transacao.Tipo.SAQUE;
                    else if ("DEPOSITO".equalsIgnoreCase(operacao)) tipo = Transacao.Tipo.DEPOSITO;
                    else {
                        linhasInvalidas++;
                        continue;
                    }

                    LocalDateTime dt = LocalDateTime.parse(datahora);

                    double valor = Double.parseDouble(valorStr);
                    if (valor <= 0.0) {
                        linhasInvalidas++;
                        continue;
                    }

                    transacoes.add(new Transacao(agencia, conta, banco, titular, tipo, dt, valor));
                } catch (Exception ex) {
                    linhasInvalidas++;
                }
            }
        }

        return transacoes;
    }
}