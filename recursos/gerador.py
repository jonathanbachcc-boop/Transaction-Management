import csv
import random
from datetime import datetime, timedelta

# Reprodutível: sempre gera o mesmo arquivo (mude a seed se quiser variar)
random.seed(42)

OUTFILE = "transacoes.csv"
N = 10_000

banco = "SANTANDER"
titulares = ["JOAO", "MARIA", "FELIPE", "JULIA", "CARLOS", "ANA", "BRUNO", "PAULA", "RAFAEL", "BIA"]
operacoes = ["SAQUE", "DEPOSITO", "TRANSFERENCIA"]

# Algumas combinações parecidas com seu exemplo
agencias = [1520, 3320, 1044, 2220, 9999]
contas = ["0001", "0002", "0004", "0007"]

start = datetime(2022, 1, 1, 0, 0, 0)
end   = datetime(2022, 2, 28, 23, 59, 59)
span_seconds = int((end - start).total_seconds())

def rand_datetime_iso():
    dt = start + timedelta(seconds=random.randint(0, span_seconds))
    return dt.strftime("%Y-%m-%dT%H:%M:%S")

def rand_valor():
    # Gera valores com 2 casas decimais, como no seu CSV
    v = random.uniform(1, 5000)
    return f"{v:.2f}"

with open(OUTFILE, "w", newline="", encoding="utf-8") as f:
    w = csv.writer(f)
    w.writerow(["AGENCIA", "CONTA", "BANCO", "TITULAR", "OPERACAO", "DATAHORA", "VALOR"])

    for _ in range(N):
        agencia = random.choice(agencias)
        conta = random.choice(contas)
        titular = random.choice(titulares)
        operacao = random.choice(operacoes)
        datahora = rand_datetime_iso()
        valor = rand_valor()

        w.writerow([agencia, conta, banco, titular, operacao, datahora, valor])

print(f"Gerado: {OUTFILE} ({N} registros)")
