# Processamento GNSS

O Datum Point trata cada ponto como uma ocupação, não como uma leitura instantânea.

Pipeline inicial:

1. aguardar permissão `ACCESS_FINE_LOCATION`;
2. usar `LocationManager` com `GPS_PROVIDER`;
3. coletar múltiplos fixes;
4. rejeitar leituras sem timestamp, sem precisão horizontal ou acima do limite configurado;
5. projetar as amostras para coordenadas métricas;
6. calcular centro robusto inicial pela mediana de E e N;
7. calcular distância radial de cada amostra ao centro;
8. estimar dispersão robusta por MAD;
9. remover outliers quando houver amostras suficientes;
10. calcular a média das amostras aceitas;
11. salvar estatísticas junto da coordenada final.

O limiar inicial de outlier é:

```text
mediana(distancia_radial) + 3.5 * 1.4826 * MAD
```

A constante `1.4826` aproxima o MAD ao desvio padrão sob distribuição normal. O filtro só é aplicado com quantidade mínima de amostras para evitar falsas rejeições em ocupações curtas.

## Estatísticas

São calculados:

- quantidade total, aceita e rejeitada;
- sigma E;
- sigma N;
- RMS radial;
- desvio radial máximo;
- precisão Android mínima, média, mediana e máxima;
- satélites visíveis/usados quando disponíveis;
- C/N0 médio quando disponível.

## Limitação

Dispersão da nuvem não é precisão absoluta. Uma ocupação pode parecer estável e ainda conter erro sistemático por multipercurso, obstruções, geometria ruim ou limitações do receptor interno do smartphone.
