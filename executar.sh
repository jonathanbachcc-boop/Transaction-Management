#!/usr/bin/env bash
# Compila e executa o projeto.
# Uso: ./executar.sh [caminho/do/arquivo.csv]
set -e
cd "$(dirname "$0")"
mkdir -p out
javac -d out $(find src -name "*.java")
java -cp out br.edu.transacoes.Main "${1:-dados/transacoes.csv}"
