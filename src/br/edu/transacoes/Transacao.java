package br.edu.transacoes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Representa uma transacao bancaria lida do arquivo CSV.
 *
 * equals/hashCode implementam a REGRA DE DUPLICATA do enunciado:
 * mesmo valor + mesmo tipo (SAQUE/DEPOSITO) + mesmo operador (conta/agencia/banco/titular)
 * + mesma data e hora exata.
 */
public final class Transacao {

    public enum Tipo { SAQUE, DEPOSITO }

    private final String agencia;
    private final String conta;
    private final String banco;
    private final String titular;
    private final Tipo tipo;
    private final LocalDateTime dataHora;
    private final double valor;

    public Transacao(String agencia, String conta, String banco, String titular,
                     Tipo tipo, LocalDateTime dataHora, double valor) {
        this.agencia = agencia;
        this.conta = conta;
        this.banco = banco;
        this.titular = titular;
        this.tipo = tipo;
        this.dataHora = dataHora;
        this.valor = valor;
    }

    public String getAgencia() { return agencia; }
    public String getConta() { return conta; }
    public String getBanco() { return banco; }
    public String getTitular() { return titular; }
    public Tipo getTipo() { return tipo; }
    public LocalDateTime getDataHora() { return dataHora; }
    public double getValor() { return valor; }

    /** Chave de agrupamento: titular + banco/agencia/conta. */
    public String chaveConta() {
        return titular + "|" + banco + "|" + agencia + "|" + conta;
    }

    /** Valor com sinal aplicado ao saldo. */
    public double impactoNoSaldo() {
        return tipo == Tipo.DEPOSITO ? valor : -valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transacao)) return false;
        Transacao t = (Transacao) o;
        return Double.compare(valor, t.valor) == 0
                && tipo == t.tipo
                && Objects.equals(dataHora, t.dataHora)
                && Objects.equals(agencia, t.agencia)
                && Objects.equals(conta, t.conta)
                && Objects.equals(banco, t.banco)
                && Objects.equals(titular, t.titular);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor, tipo, dataHora, agencia, conta, banco, titular);
    }

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Override
    public String toString() {
        return String.format("%s | ag %s cc %s | %-8s | R$ %10.2f",
                FMT.format(dataHora), agencia, conta, tipo, valor);
    }
}
