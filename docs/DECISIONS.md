# Decisões técnicas

## ADR-001: Módulo Android único na primeira versão

A especificação sugere múltiplos módulos. A versão inicial usa um módulo `app` único para reduzir custo de bootstrap e manter a compilação simples. A separação de responsabilidades foi preservada por pacotes `core`, `data`, `gnss`, `processing`, `export` e `ui`.

## ADR-002: Room 2.8.5

Foi escolhida a linha AndroidX Room 2.x estável (`2.8.5`) em vez de migrar imediatamente para Room 3.0.3. Isso mantém compatibilidade mais previsível para um app Android puro e evita introduzir a migração KMP como risco inicial.

## ADR-003: UTM implementado diretamente

A projeção SIRGAS 2000 / UTM 22S foi implementada diretamente com parâmetros GRS80. A decisão evita incluir uma base EPSG grande na primeira versão e mantém o cálculo auditável e testável.

## ADR-004: Captura fake visível no v0.1.0

A UI inicial usa captura simulada para permitir desenvolvimento e testes sem permanência em campo. A fonte real `LocationManager` / `GPS_PROVIDER` está encapsulada em `GnssLocationSource` e deve ser conectada ao fluxo de permissões completo antes do v1.0.

## ADR-005: PDF e KMZ adiados

CSV, TXT, GeoJSON, KML e DXF foram implementados primeiro por serem auditáveis e testáveis em unidade. PDF e KMZ exigem integração Android de arquivo/relatório e permanecem como item de fechamento da v1.0.
