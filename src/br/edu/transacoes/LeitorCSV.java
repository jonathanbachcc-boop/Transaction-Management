package br.edu.transacoes;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Le o arquivo CSV de transacoes.
 *
 * Layout esperado (o VALOR foi adicionado ao layout original, como pede o enunciado):
 * AGENCIA,CONTA,BANCO,TITULAR,OPERACAO,DATAHORA,VALOR
 *
 * Linhas invalidas (data malformada, valor nao numerico, tipo desconhecido)
 * sao descartadas e contabilizadas em {@link #getLinhasInvalidas()}.
 *
 * Complexidade: O(n) para n linhas do arquivo.
 */
public class LeitorCSV {

    private int linhasInvalidas = 0;

    public List<Transacao> ler(Path arquivo) throws IOException {
        // ArrayList: insercao amortizada O(1) e acesso O(1) por indice,
        // o que interessa para o merge sort aplicado depois.
        List<Transacao> transacoes = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(arquivo, StandardCharsets.UTF_8)) {
            String linha = reader.readLine(); // cabecalho
            while ((linha = reader.readLine()) != null) {
                if (linha.isBlank()) continue;
                Transacao t = converter(linha);
                if (t == null) {
                    linhasInvalidas++;
                } else {
                    transacoes.add(t);
                }
            }
        }
        return transacoes;
    }

    private Transacao converter(String linha) {
        String[] c = linha.split(",", -1);
        if (c.length < 7) return null;
        try {
            String agencia = c[0].trim();
            String conta = c[1].trim();
            String banco = c[2].trim().toUpperCase();
            String titular = c[3].trim().toUpperCase();
            Transacao.Tipo tipo = Transacao.Tipo.valueOf(c[4].trim().toUpperCase());
            LocalDateTime dataHora = LocalDateTime.parse(c[5].trim());
            double valor = Double.parseDouble(c[6].trim().replace("\"", ""));
            if (valor <= 0) return null; // regra de validacao de valor
            return new Transacao(agencia, conta, banco, titular, tipo, dataHora, valor);
        } catch (IllegalArgumentException | DateTimeParseException e) {
            return null;
        }
    }

    public int getLinhasInvalidas() {
        return linhasInvalidas;
    }
}
