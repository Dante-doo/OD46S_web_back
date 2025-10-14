#!/bin/bash

# Script para gerar relatório de cobertura de código
# Autor: Sistema OD46S
# Data: 2025-10-14

echo "🔍 Gerando relatório de cobertura de código..."

# Limpar e executar testes com cobertura
echo "📊 Executando testes com cobertura..."
./mvnw clean test jacoco:report

# Verificar se o relatório foi gerado
if [ -f "target/site/jacoco/index.html" ]; then
    echo "✅ Relatório de cobertura gerado com sucesso!"
    echo "📁 Localização: target/site/jacoco/index.html"
    
    # Extrair métricas do CSV
    if [ -f "target/site/jacoco/jacoco.csv" ]; then
        echo ""
        echo "📈 MÉTRICAS DE COBERTURA:"
        echo "========================="
        
        # Calcular cobertura total
        TOTAL_INSTRUCTIONS=$(tail -n +2 target/site/jacoco/jacoco.csv | awk -F',' '{sum += $4 + $5} END {print sum}')
        COVERED_INSTRUCTIONS=$(tail -n +2 target/site/jacoco/jacoco.csv | awk -F',' '{sum += $5} END {print sum}')
        COVERAGE_PERCENT=$(echo "scale=2; $COVERED_INSTRUCTIONS * 100 / $TOTAL_INSTRUCTIONS" | bc)
        
        echo "📊 Cobertura de Instruções: ${COVERAGE_PERCENT}%"
        echo "📊 Instruções Totais: $TOTAL_INSTRUCTIONS"
        echo "📊 Instruções Cobertas: $COVERED_INSTRUCTIONS"
        echo "📊 Instruções Não Cobertas: $((TOTAL_INSTRUCTIONS - COVERED_INSTRUCTIONS))"
        
        # Verificar se atinge 80%
        if (( $(echo "$COVERAGE_PERCENT >= 80" | bc -l) )); then
            echo "🎯 ✅ Meta de 80% de cobertura ATINGIDA!"
        else
            echo "⚠️  Meta de 80% de cobertura NÃO atingida (${COVERAGE_PERCENT}%)"
        fi
        
        echo ""
        echo "📋 COBERTURA POR PACOTE:"
        echo "========================"
        tail -n +2 target/site/jacoco/jacoco.csv | while IFS=',' read -r group package class missed covered branch_missed branch_covered line_missed line_covered complexity_missed complexity_covered method_missed method_covered; do
            if [ "$package" != "" ] && [ "$class" = "" ]; then
                total=$((missed + covered))
                if [ $total -gt 0 ]; then
                    percent=$(echo "scale=1; $covered * 100 / $total" | bc)
                    echo "📦 $package: ${percent}%"
                fi
            fi
        done
        
    else
        echo "❌ Arquivo CSV não encontrado"
    fi
    
    echo ""
    echo "🌐 Para visualizar o relatório completo, abra:"
    echo "   file://$(pwd)/target/site/jacoco/index.html"
    
else
    echo "❌ Erro ao gerar relatório de cobertura"
    exit 1
fi

echo ""
echo "🎯 Relatório de cobertura concluído!"
