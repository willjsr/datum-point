# Datum Point

Datum Point é um aplicativo Android open source para reconhecimento e conferência GNSS em campo com smartphone.

Ele coleta múltiplas posições GNSS, filtra leituras inadequadas, calcula uma coordenada representativa, registra estatísticas de dispersão e exporta resultados em formatos úteis para topografia, CAD e GIS.

## Estado

Versão inicial: `0.1.0`.

Esta base inclui:

- Android Kotlin + Jetpack Compose + Material 3;
- domínio de projetos e pontos;
- projeção prática WGS84 para SIRGAS 2000 / UTM 22S;
- estatística robusta com MAD para outliers;
- cálculo de distância, azimute, perímetro e área;
- exportadores CSV, TXT, GeoJSON, KML e DXF;
- entidades Room e DAOs;
- fonte GNSS real via `LocationManager` / `GPS_PROVIDER`;
- fonte GNSS fake para testes e previews;
- documentação, licença MIT e CI.

## Limitação técnica essencial

Datum Point não é receptor GNSS RTK, não substitui estação total, PPK, levantamento cadastral de precisão ou georreferenciamento oficial.

A média de posições melhora a análise de estabilidade da ocupação, mas não transforma o GNSS interno do smartphone em equipamento centimétrico. Diferenças próximas da incerteza GNSS devem ser tratadas como inconclusivas.

## Privacidade

- Sem login.
- Sem analytics.
- Sem anúncios.
- Sem backend.
- Coordenadas ficam no dispositivo por padrão.

## Como compilar

Requisitos:

- JDK 17 ou superior;
- Android SDK com API 37;
- Gradle Wrapper do projeto.

Comandos:

```bash
./gradlew test
./gradlew lint
./gradlew assembleDebug
```

O APK debug será gerado em:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Como usar

1. Crie ou abra um projeto.
2. Use **OCUPAR NOVO PONTO**.
3. Em aparelho real, conceda localização precisa.
4. Aguarde estabilização e observe por 60 segundos.
5. Revise coordenadas, dispersão, RMS, precisão reportada e satélites.
6. Salve o ponto.
7. Repita para outros pontos.
8. Use Croqui, Comparação e Exportação.

## Coordenadas

A coordenada geográfica original WGS84 é preservada em `Double`.

O sistema projetado padrão é SIRGAS 2000 / UTM zona 22S, EPSG:31982, implementado com parâmetros GRS80 e fórmulas UTM auditáveis.

## Formatos

- CSV;
- TXT;
- GeoJSON;
- KML;
- DXF ASCII simples.

KMZ e PDF estão planejados na arquitetura de exportação, mas ainda dependem da integração Android de arquivo/relatório.

## Licença

Código original do Datum Point: MIT.

Dependências mantêm suas próprias licenças.
