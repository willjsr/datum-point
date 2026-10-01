# Datum Point — Especificação completa para implementação autônoma

## 0. Instrução principal ao agente

Você é o agente responsável por **projetar, implementar, testar, documentar e deixar pronto para publicação open source** um aplicativo Android chamado **Datum Point**.

Este documento já funciona como autorização para executar **todas as etapas necessárias** do projeto do início ao fim.

### Modo de execução obrigatório

- **Não pare após criar um plano.**
- **Não peça autorização entre etapas normais de implementação.**
- **Não transforme este trabalho em uma sequência de aprovações manuais.**
- Leia esta especificação inteira, monte internamente a ordem de execução e **comece a implementar imediatamente**.
- Execute sequencialmente todas as etapas descritas neste documento até atingir a definição de pronto.
- Quando encontrar uma decisão técnica não especificada:
  1. escolha a alternativa mais simples, estável, moderna e compatível com software open source;
  2. implemente;
  3. registre a decisão em `docs/DECISIONS.md`;
  4. continue sem interromper o trabalho.
- Só interrompa por algo que seja realmente impossível continuar sem ação externa, como credencial secreta indispensável, acesso negado ao sistema ou operação destrutiva fora do repositório.
- Se uma dependência, versão ou API citada aqui estiver desatualizada, use a **versão estável compatível mais recente** disponível no momento da implementação e documente a alteração.
- Sempre que uma etapa gerar erro de compilação, teste, lint ou integração, investigue e corrija automaticamente antes de avançar.
- Se o repositório Git estiver disponível, faça commits lógicos ao longo da implementação.
- Se houver acesso autorizado ao GitHub e um remoto configurado, deixe o repositório preparado para push/publicação. Não exponha chaves ou credenciais.
- Ao terminar, forneça um resumo objetivo do que foi criado, testes executados, limitações conhecidas e localização do APK gerado.

---

# 1. Projeto

**Nome:** Datum Point  
**Repositório sugerido:** `datum-point`  
**Plataforma inicial:** Android  
**Linguagem:** Kotlin  
**UI:** Jetpack Compose  
**Licença do código do projeto:** MIT  
**Modelo:** open source, offline-first e privacy-first  
**Package/namespace inicial sugerido:** `org.datumpoint.app`

O aplicativo deve ser utilizável principalmente em smartphones Android modernos, tendo como aparelho inicial de referência um **Samsung Galaxy S25**, mas sem criar dependência específica de hardware Samsung.

---

# 2. Objetivo do Datum Point

O Datum Point é uma ferramenta de **levantamento GNSS de reconhecimento e conferência em campo**.

O usuário deve conseguir:

1. criar um projeto;
2. ir fisicamente até um ponto ou vértice;
3. posicionar o smartphone sobre ou próximo ao local desejado;
4. iniciar uma ocupação GNSS;
5. observar o ponto durante determinado período;
6. coletar múltiplas posições GNSS em vez de salvar apenas uma leitura instantânea;
7. filtrar leituras inadequadas;
8. calcular uma coordenada representativa do conjunto observado;
9. registrar informações de qualidade e dispersão;
10. salvar latitude/longitude e coordenadas projetadas;
11. repetir o procedimento para vários pontos;
12. visualizar a geometria resultante;
13. calcular distâncias, azimutes, perímetro e área;
14. comparar medidas levantadas com valores de referência de um projeto existente;
15. exportar os dados em formatos úteis para topografia, CAD e GIS.

O aplicativo é destinado principalmente a:

- conferência preliminar de vértices;
- identificação de divergências grosseiras;
- reconhecimento de campo;
- verificação aproximada de implantação;
- apoio a levantamentos;
- documentação de visitas técnicas;
- comparação preliminar entre situação de campo e projeto.

---

# 3. Limitação fundamental e comunicação ao usuário

O Datum Point **NÃO é um receptor GNSS RTK** e não deve se apresentar como substituto de:

- receptor GNSS geodésico;
- RTK;
- PPK;
- estação total;
- levantamento cadastral de precisão;
- levantamento destinado diretamente a certificação, registro ou georreferenciamento oficial.

A interface e a documentação devem deixar claro que:

- a posição é obtida pelo GNSS interno do smartphone;
- erros sistemáticos podem permanecer mesmo durante uma ocupação longa;
- média de posições melhora a análise de estabilidade, mas não transforma o smartphone em equipamento centimétrico;
- construções, muros, árvores e outras superfícies podem produzir multipercurso;
- a coordenada calculada corresponde aproximadamente à posição da antena GNSS do aparelho, e não automaticamente ao vértice físico onde o celular foi apoiado;
- casas decimais adicionais não significam precisão adicional;
- diferenças pequenas, próximas da incerteza da observação, devem ser consideradas inconclusivas.

Nunca exibir frases como:

- “precisão centimétrica”;
- “precisão topográfica”;
- “levantamento georreferenciado”;
- “coordenada oficial”;

a menos que futuramente exista integração real com hardware externo capaz de justificá-las.

---

# 4. Princípios de produto

## 4.1 Offline-first

As funções essenciais devem funcionar sem internet:

- criar projeto;
- ocupar ponto;
- salvar pontos;
- calcular coordenadas;
- calcular geometria;
- comparar medidas;
- visualizar croqui local;
- exportar arquivos.

Nenhum login deve ser necessário.

## 4.2 Privacy-first

Por padrão:

- nenhuma coordenada deve ser enviada para servidor;
- não utilizar analytics;
- não utilizar anúncios;
- não utilizar telemetria;
- não exigir conta;
- não exigir backend.

Os dados devem ficar armazenados localmente no aparelho.

## 4.3 Open source real

O projeto deve ser fácil de:

- clonar;
- compilar;
- auditar;
- modificar;
- distribuir;
- contribuir.

Evitar dependências proprietárias quando existir alternativa open source adequada.

---

# 5. Stack técnica

Adotar preferencialmente:

- Kotlin;
- Jetpack Compose;
- Material 3;
- Android Architecture Components;
- ViewModel;
- Kotlin Coroutines;
- Flow / StateFlow;
- Room para projetos, pontos, sessões e referências;
- DataStore para configurações;
- Navigation Compose;
- Gradle Kotlin DSL;
- JUnit para testes unitários;
- testes instrumentados quando necessários;
- GitHub Actions para CI.

Usar as versões estáveis e compatíveis disponíveis no momento da implementação.

### SDK

- `minSdk`: preferencialmente API 26 ou superior;
- `compileSdk`: SDK estável atual;
- `targetSdk`: SDK estável atual.

Se a toolchain exigir ajuste, fazer o ajuste e registrar em `docs/DECISIONS.md`.

---

# 6. Arquitetura

Utilizar arquitetura em camadas com fluxo unidirecional de estado.

Estrutura sugerida:

```text
datum-point/
├── app/
├── core/
│   ├── data/
│   ├── database/
│   ├── gnss/
│   ├── geodesy/
│   ├── export/
│   └── ui/
├── feature/
│   ├── projects/
│   ├── capture/
│   ├── points/
│   ├── geometry/
│   ├── comparison/
│   ├── export/
│   ├── settings/
│   └── about/
├── docs/
├── .github/
├── LICENSE
├── README.md
├── README.en.md
├── CONTRIBUTING.md
├── CODE_OF_CONDUCT.md
├── SECURITY.md
├── CHANGELOG.md
└── THIRD_PARTY_NOTICES.md
```

Se a modularização acima prejudicar desnecessariamente a velocidade ou estabilidade inicial do projeto, é aceitável consolidar módulos, desde que a separação de responsabilidades permaneça clara.

---

# 7. Fonte de localização

Para a medição principal dos pontos, utilizar o GNSS real do Android.

Priorizar:

```text
LocationManager
GPS_PROVIDER
```

Não usar posição de rede, Wi-Fi ou localização aproximada como coordenada principal de um ponto de levantamento.

A aplicação pode usar outras fontes apenas para interface auxiliar, nunca misturando silenciosamente uma posição de rede com a solução GNSS registrada.

Registrar quando possível:

- latitude;
- longitude;
- altitude GNSS;
- `Location.accuracy`;
- velocidade;
- bearing;
- timestamp;
- elapsed realtime;
- provider;
- número de satélites visíveis;
- número de satélites usados no fix;
- C/N0;
- constelações observadas;
- frequência da portadora quando disponível;
- presença de sinais multifrequência quando detectável.

---

# 8. GNSS bruto

Criar suporte de diagnóstico para as APIs Android de GNSS bruto quando o aparelho oferecer essa funcionalidade.

Usar, quando suportado:

- `GnssMeasurementsEvent.Callback`;
- `GnssStatus.Callback`;
- informações de `GnssMeasurement`;
- listener NMEA, se tecnicamente adequado.

O aplicativo deve fazer **feature detection em tempo de execução**.

Nunca assumir que todo Android possui suporte a todas as medições.

Se GNSS bruto não estiver disponível:

- o modo normal de ocupação deve continuar funcionando;
- informar “Medições GNSS brutas não disponíveis neste dispositivo”.

## 8.1 Escopo da versão inicial

Na primeira versão, o GNSS bruto deve ser usado principalmente para:

- diagnóstico;
- estatísticas;
- identificação de constelações;
- C/N0;
- frequência;
- registro opcional de dados avançados.

**Não é obrigatório implementar uma solução própria de pseudodistâncias na v1.0.**

A coordenada principal pode continuar baseada nas soluções GNSS fornecidas pelo `LocationManager`, desde que seja aplicada a metodologia de ocupação e filtragem desta especificação.

---

# 9. Fluxo de ocupação de um ponto

O botão principal deve ser:

**OCUPAR PONTO**

## 9.1 Estados

A ocupação deve possuir estados claramente visíveis:

```text
Aguardando GNSS
↓
Estabilizando
↓
Observando
↓
Processando
↓
Resultado
↓
Salvar / Repetir
```

## 9.2 Estabilização

Antes de começar a contar a observação efetiva:

- aguardar GNSS disponível;
- exigir `ACCESS_FINE_LOCATION`;
- confirmar GPS habilitado;
- coletar algumas posições;
- verificar se há solução utilizável;
- preferencialmente aguardar pelo menos alguns segundos de estabilização.

Sugestão inicial:

- mínimo de 10 segundos de warm-up;
- mínimo de 5 fixes GNSS válidos;
- precisão horizontal reportada inferior a um limite de aquisição, inicialmente 15 m;
- pelo menos 4 satélites usados no fix quando essa informação estiver disponível.

Se o sinal continuar ruim, o aplicativo deve explicar o problema, mas permitir ao usuário continuar conscientemente em modo de baixa qualidade.

---

# 10. Duração de observação

Criar presets:

- 30 s;
- 60 s;
- 120 s;
- 180 s.

Padrão inicial:

**60 segundos**

Permitir configuração nas preferências.

Durante a observação mostrar em tempo real:

- nome do ponto;
- tempo decorrido;
- tempo restante;
- quantidade de fixes recebidos;
- fixes aceitos;
- fixes rejeitados;
- precisão reportada atual;
- precisão reportada mediana;
- satélites visíveis;
- satélites usados no fix;
- constelações;
- C/N0 médio/mediano quando disponível;
- estabilidade da ocupação;
- coordenada provisória;
- pequeno gráfico ou indicador de dispersão, se viável.

Manter a tela acordada durante a ocupação.

---

# 11. Filtro de amostras

Cada fix recebido deve ser armazenado temporariamente na sessão.

Rejeitar ou marcar como inválido quando:

- não for proveniente do GNSS esperado;
- possuir timestamp inválido;
- for excessivamente antigo;
- não possuir precisão horizontal válida;
- `accuracy` exceder o limite configurado.

Limite padrão sugerido:

**10 m**

Esse valor deve ser configurável.

Não apagar silenciosamente as leituras rejeitadas do diagnóstico da sessão. Registrar:

- total recebido;
- total aceito;
- total rejeitado;
- motivo da rejeição.

---

# 12. Detecção de outliers

Após coletar quantidade suficiente de amostras:

1. transformar as posições para um sistema métrico adequado;
2. calcular centro robusto inicial, preferencialmente baseado em mediana;
3. calcular distância radial de cada amostra ao centro;
4. usar uma técnica robusta como MAD — Median Absolute Deviation;
5. classificar outliers com limiar estatístico documentado;
6. somente aplicar rejeição estatística se houver quantidade mínima suficiente de amostras;
7. registrar quantas amostras foram removidas pelo filtro de outliers.

Não utilizar um algoritmo que artificialmente faça a nuvem parecer mais precisa do que realmente é.

O algoritmo utilizado deve ser explicado em:

`docs/GNSS_PROCESSING.md`

---

# 13. Coordenada final do ponto

A posição final deve ser calculada somente com amostras válidas após filtros.

Preferir um método simples, auditável e testável.

Sugestão:

- centro robusto para detecção de outliers;
- média aritmética das amostras aceitas como coordenada final.

Se for implementada ponderação por precisão reportada, documentar claramente a equação utilizada e manter testes específicos.

Salvar também estatísticas da nuvem para que a coordenada final nunca fique desacompanhada de informações de qualidade.

---

# 14. Estatísticas obrigatórias por ocupação

Salvar:

- quantidade total de fixes;
- quantidade aceita;
- quantidade rejeitada;
- duração;
- latitude média;
- longitude média;
- Easting final;
- Northing final;
- altitude GNSS média, se disponível;
- desvio padrão em E;
- desvio padrão em N;
- RMS radial;
- desvio radial máximo;
- precisão Android mínima;
- precisão Android média;
- precisão Android mediana;
- precisão Android máxima;
- satélites visíveis;
- satélites usados no fix;
- C/N0 médio;
- C/N0 mediano;
- constelações observadas;
- data/hora;
- modelo do aparelho;
- versão Android;
- indicador de suporte a GNSS bruto;
- observações do usuário.

Usar nomes como:

- **dispersão**;
- **estabilidade**;
- **precisão reportada pelo Android**;

e evitar chamar o desvio da nuvem de “precisão absoluta”.

---

# 15. Indicador de qualidade

Criar um indicador visual simples:

- **Estável**
- **Moderado**
- **Fraco**

O indicador deve considerar múltiplos fatores, por exemplo:

- número de amostras aceitas;
- `Location.accuracy` mediana;
- RMS radial;
- quantidade de satélites usados;
- duração;
- presença de outliers.

Os limites devem ficar centralizados em configuração/código facilmente ajustável e documentados.

Mostrar sempre:

> “Indicador de estabilidade da ocupação. Não representa precisão topográfica certificada.”

---

# 16. Sistemas de coordenadas

## 16.1 Coordenada primária armazenada

Sempre preservar a posição geográfica original:

**WGS84 / latitude e longitude**

Armazenar em `Double`.

## 16.2 Sistema projetado padrão

Para o uso inicial em Curitiba e Região Metropolitana:

**SIRGAS 2000 / UTM zona 22S**

Referência:

**EPSG:31982**

Exibir:

```text
E: 000000.000 m
N: 0000000.000 m
```

## 16.3 Implementação

É aceitável utilizar **Proj4J core**, ou outra biblioteca permissiva e confiável, desde que:

- a licença seja compatível;
- a dependência fique documentada;
- existam testes de transformação;
- não seja adicionada uma grande base EPSG apenas por conveniência se ela não for necessária.

Para o caso padrão, é permitido definir explicitamente a projeção UTM zona 22 sul no elipsoide GRS80, evitando dependência desnecessária de banco EPSG.

## 16.4 Transparência de datum

Como o GNSS do smartphone fornece uma solução vinculada ao referencial global utilizado pelo sistema, a conversão apresentada como SIRGAS 2000 deve conter uma observação técnica na documentação explicando que esta é uma transformação prática para reconhecimento de campo e **não uma materialização geodésica oficial de época**.

Nunca insinuar precisão geodésica inexistente.

## 16.5 Expansão

Projetar a classe de CRS para futuramente aceitar:

- outras zonas UTM;
- seleção automática de zona;
- outros EPSG;
- hemisfério norte/sul.

Não hardcodar a aplicação inteira exclusivamente em 22S.

---

# 17. Casas decimais

Permitir exibir:

- latitude/longitude: até 8 casas decimais;
- Easting/Northing: até 3 casas decimais.

Entretanto, exibir na interface uma mensagem educativa:

> “Número de casas decimais não representa a precisão real da medição.”

Os cálculos devem utilizar os valores internos completos.

---

# 18. Modelo de dados

Criar entidades equivalentes a:

## Project

```text
id
name
description
createdAt
updatedAt
crs
defaultObservationSeconds
notes
```

## SurveyPoint

```text
id
projectId
name
description
sequence
latitude
longitude
easting
northing
altitude
createdAt
qualityClass
notes
```

## ObservationSession

```text
id
pointId
startedAt
endedAt
durationSeconds
totalFixes
acceptedFixes
rejectedFixes
sigmaE
sigmaN
radialRms
maxRadialDeviation
minReportedAccuracy
meanReportedAccuracy
medianReportedAccuracy
maxReportedAccuracy
satellitesVisible
satellitesUsed
meanCn0
medianCn0
rawGnssSupported
deviceModel
androidVersion
processingVersion
```

## LocationSample

Não é obrigatório persistir para sempre todas as amostras no banco principal.

Permitir estratégia configurável:

- manter resumo;
- manter amostras da sessão;
- apagar amostras após processamento;
- exportar log avançado.

## ReferenceSegment

```text
id
projectId
fromPointName
toPointName
expectedDistanceMeters
description
```

## ReferenceValue

Para referências adicionais:

```text
id
projectId
type
value
unit
description
```

Tipos possíveis:

- AREA;
- PERIMETER;
- DISTANCE.

---

# 19. Nomeação de pontos

Ao criar um projeto novo, sugerir automaticamente:

```text
P01
P02
P03
P04
...
```

Permitir alterar para:

- 01;
- V01;
- M1;
- A;
- qualquer nome definido pelo usuário.

Não sobrescrever ponto existente sem confirmação explícita.

---

# 20. Tela de projetos

Tela inicial:

- logotipo/nome Datum Point;
- lista de projetos;
- botão “Novo projeto”;
- data da última modificação;
- quantidade de pontos;
- menu de exportação/duplicação/exclusão.

Permitir pesquisar projeto pelo nome.

---

# 21. Tela do projeto

Mostrar:

- nome;
- CRS;
- quantidade de pontos;
- área, se houver polígono válido;
- perímetro;
- lista ordenada de pontos;
- botão destacado **OCUPAR NOVO PONTO**;
- botão “Croqui”;
- botão “Comparar”;
- botão “Exportar”.

Cada item da lista deve mostrar resumidamente:

```text
P03
E: 674532.284
N: 7184991.763
Estabilidade: Moderada
60 s • 53 amostras
```

---

# 22. Tela de captura

Esta é a principal tela operacional.

Exibir em tamanho grande:

```text
PONTO P03

GNSS: ATIVO
Estabilidade: MODERADA

Satélites visíveis: 31
Usados no fix: 19
Precisão reportada: 3.2 m
Dispersão atual: 1.4 m

E provisório: ...
N provisório: ...

00:43 / 01:00
```

Botões:

- Cancelar;
- Pausar, somente se tecnicamente consistente;
- Finalizar antes, com confirmação;
- Repetir;
- Salvar.

A interface deve ser fácil de usar ao ar livre.

Priorizar:

- números grandes;
- alto contraste;
- poucos toques;
- feedback visual claro.

---

# 23. Detalhes do ponto

Mostrar:

## Coordenadas

- latitude;
- longitude;
- Easting;
- Northing;
- altitude GNSS.

## Ocupação

- duração;
- fixes;
- filtros;
- RMS;
- sigma E/N;
- precisão reportada;
- satélites;
- constelações;
- C/N0.

## Metadados

- data/hora;
- dispositivo;
- notas.

Ações:

- editar nome;
- editar descrição;
- repetir ocupação;
- excluir;
- compartilhar;
- exportar ponto.

Se uma nova ocupação substituir a anterior, preservar histórico quando possível.

---

# 24. Croqui / geometria

Criar uma tela de visualização 2D offline usando `Canvas` do Compose ou mecanismo equivalente.

Não depender de mapa online para a funcionalidade principal.

Mostrar:

- pontos;
- nomes;
- segmentos;
- norte;
- escala aproximada;
- polígono fechado;
- dimensões dos lados;
- opção de zoom/pan;
- destacar ponto selecionado.

Ajustar automaticamente a geometria à tela.

---

# 25. Cálculos geométricos

A partir de Easting/Northing:

## Distância

```text
d = sqrt((E2-E1)^2 + (N2-N1)^2)
```

## Azimute

Calcular azimute de grade:

- origem no norte;
- sentido horário;
- intervalo 0° a 360°.

## Área

Usar fórmula de Shoelace para polígono projetado.

Mostrar:

- m²;
- hectares.

## Perímetro

Somar todos os segmentos, incluindo fechamento do último ponto com o primeiro.

## Ordem

Permitir reordenar os pontos manualmente.

Não calcular área como válida se houver menos de 3 pontos.

---

# 26. Comparação com projeto existente

Uma das finalidades centrais é identificar divergências preliminares entre o que foi observado em campo e o que está representado em um projeto.

Criar a tela **Comparação**.

## 26.1 Comparação por distâncias

Permitir cadastrar referências, por exemplo:

```text
P01 → P02 = 18.40 m
P02 → P03 = 32.15 m
```

Após levantamento, mostrar:

```text
Trecho       Projeto     Datum Point     Diferença
P01-P02      18.40 m     19.46 m         +1.06 m
P02-P03      32.15 m     33.84 m         +1.69 m
```

Adicionar aviso:

> “Diferenças próximas da incerteza GNSS são inconclusivas.”

## 26.2 Área e perímetro de referência

Permitir informar:

```text
Área de projeto: 523.41 m²
Perímetro de projeto: 96.82 m
```

Mostrar comparação com o polígono observado.

## 26.3 Coordenadas de referência

Permitir opcionalmente importar CSV de coordenadas conhecidas.

Formato mínimo:

```csv
name,easting,northing
P01,674500.320,7184500.410
P02,674519.760,7184501.180
```

O usuário deve informar o CRS do arquivo.

Se o CRS da referência não for compatível ou conhecido, não calcular deslocamentos absolutos silenciosamente.

Quando houver correspondência por nome, calcular:

- ΔE;
- ΔN;
- deslocamento horizontal;
- diferença de distância entre segmentos.

---

# 27. Importações

Versão inicial obrigatória:

- CSV de coordenadas de referência.

Desejável, caso não comprometa a estabilidade:

- GeoJSON.

Deixar arquitetura preparada para futuramente aceitar:

- KML;
- DXF.

Não tornar DXF uma dependência para concluir a v1.0.

---

# 28. Exportações

Implementar:

## CSV

Campos mínimos:

```text
project
point
sequence
latitude
longitude
easting
northing
altitude
crs
timestamp
duration_seconds
accepted_fixes
rejected_fixes
reported_accuracy_median
sigma_e
sigma_n
radial_rms
satellites_used
quality
notes
```

## TXT

Relatório legível com:

- dados do projeto;
- sistema de coordenadas;
- tabela de pontos;
- distâncias;
- azimutes;
- área;
- perímetro;
- observações de qualidade.

## GeoJSON

- FeatureCollection;
- Point para cada ponto;
- LineString;
- Polygon quando aplicável;
- propriedades completas.

## KML

- Placemark dos pontos;
- LineString;
- Polygon;
- nomes e descrições.

## KMZ

Compactar KML quando selecionado.

## DXF

Produzir DXF simples e interoperável.

Incluir:

- `POINT`;
- `TEXT` para identificação;
- `LWPOLYLINE` ou equivalente para o perímetro.

Utilizar Easting/Northing como coordenadas CAD.

Preferir um DXF ASCII simples, auditável e sem dependência desnecessariamente pesada.

## PDF

Criar relatório PDF básico usando API Android adequada.

Conteúdo:

- Datum Point;
- nome do projeto;
- data;
- CRS;
- aviso de finalidade;
- tabela de pontos;
- qualidade por ponto;
- distâncias;
- azimutes;
- área;
- perímetro;
- comparação de referências;
- croqui simples.

Rodapé obrigatório:

> “Levantamento GNSS de reconhecimento realizado com smartphone. Este relatório não substitui levantamento topográfico/geodésico executado com equipamento e metodologia apropriados.”

---

# 29. Compartilhamento e armazenamento de arquivos

Usar APIs modernas do Android:

- Storage Access Framework;
- Share Sheet;
- URIs seguras;
- FileProvider quando necessário.

Não solicitar permissões legadas de armazenamento desnecessárias.

Permitir ao usuário escolher a pasta de destino.

---

# 30. Log GNSS avançado

Criar em Configurações:

**Registrar dados GNSS avançados**

Desligado por padrão.

Quando ligado, permitir salvar um arquivo de diagnóstico da sessão contendo, quando disponíveis:

- epoch;
- svid;
- constelação;
- C/N0;
- frequência;
- estado da medição;
- pseudodistance rate;
- accumulated delta range;
- dados de clock necessários para análise posterior;
- NMEA opcional.

Não é obrigatório interpretar todos esses valores na interface.

O objetivo é permitir futura análise e evolução do projeto.

---

# 31. Permissões Android

Solicitar apenas o necessário.

Prováveis permissões:

- `ACCESS_FINE_LOCATION`;
- `ACCESS_COARSE_LOCATION`, se exigido pelo fluxo Android;
- `FOREGROUND_SERVICE`, se houver serviço;
- `FOREGROUND_SERVICE_LOCATION`, quando aplicável;
- `POST_NOTIFICATIONS` quando necessário para notificação do serviço.

Evitar `ACCESS_BACKGROUND_LOCATION` na v1.0 se a ocupação puder funcionar com um foreground service iniciado enquanto o app está visível.

Não solicitar permissões sem explicação.

Antes do prompt de permissão, exibir racional curto:

> “O Datum Point precisa da localização precisa para registrar observações GNSS de campo. Os dados ficam armazenados localmente no aparelho.”

---

# 32. Foreground service

Se necessário para impedir que a coleta seja interrompida quando a tela apagar ou o usuário trocar brevemente de aplicativo, implementar foreground service do tipo `location`.

O serviço deve:

- iniciar somente por ação explícita do usuário;
- mostrar notificação persistente;
- informar ponto e tempo de ocupação;
- permitir retornar à captura;
- parar ao terminar/cancelar a observação.

Não iniciar monitoramento permanente.

---

# 33. Configurações

Criar opções para:

- duração padrão;
- limite máximo de `accuracy`;
- tempo de warm-up;
- CRS padrão;
- zona UTM;
- número de casas decimais;
- manter tela ligada;
- registrar GNSS bruto;
- salvar amostras;
- tema:
  - sistema;
  - claro;
  - escuro;
- unidade de área:
  - m²;
  - ha;
  - ambas.

Persistir com DataStore.

---

# 34. Internacionalização

Não hardcodar textos em Composables.

Usar resources.

Idioma inicial:

**pt-BR**

Preparar estrutura para:

**en-US**

Criar `README.md` prioritariamente em português e `README.en.md` em inglês.

---

# 35. Acessibilidade

Implementar:

- contraste adequado;
- content descriptions;
- alvos de toque apropriados;
- suporte a escala de fonte;
- não depender exclusivamente de cor para indicar qualidade.

---

# 36. Tema visual

Visual profissional de ferramenta de campo.

Direção:

- minimalista;
- técnico;
- limpo;
- alta legibilidade;
- Material 3;
- excelente dark mode.

Criar ícone vetorial próprio e simples para o projeto, por exemplo:

- ponto de levantamento;
- mira/crosshair;
- marcador;
- referência a datum/coordenadas.

Não copiar logotipos de terceiros.

---

# 37. Tela Sobre

Mostrar:

- Datum Point;
- versão;
- licença MIT;
- link/referência ao repositório quando configurado;
- privacidade;
- limitações técnicas;
- bibliotecas de terceiros;
- créditos.

Adicionar seção:

**O que este aplicativo não é**

Explicar claramente que não substitui RTK/estação total.

---

# 38. Banco de dados e migrações

Usar Room.

Requisitos:

- migrations versionadas;
- nada de `fallbackToDestructiveMigration` em release;
- testes de migration quando o schema evoluir;
- índices onde necessário;
- cascade controlado;
- export/import não deve depender diretamente da representação interna da Room.

---

# 39. Qualidade do código

Configurar:

- Kotlin formatting;
- lint;
- Detekt ou equivalente;
- `.editorconfig`;
- warnings tratados sempre que razoável;
- documentação das partes matemáticas;
- nomes claros;
- evitar classes gigantes.

Não introduzir framework complexo sem necessidade.

---

# 40. Testes obrigatórios

## 40.1 Geodésia

Testar:

- lat/lon → UTM;
- UTM → lat/lon se implementado;
- zona 22S;
- pontos em diferentes quadrantes da área de uso;
- round-trip;
- limites de zona;
- hemisfério.

Comparar resultados com uma implementação de referência confiável.

## 40.2 Geometria

Testar:

- distância;
- azimute;
- normalização 0–360;
- área;
- perímetro;
- polígono com ordem invertida;
- menos de 3 pontos;
- ponto duplicado.

## 40.3 Estatística

Testar:

- média;
- mediana;
- desvio padrão;
- RMS;
- MAD;
- remoção de outliers;
- zero amostras;
- uma amostra;
- poucas amostras;
- grande outlier.

## 40.4 Import/export

Validar:

- CSV;
- GeoJSON;
- KML;
- DXF;
- TXT.

Garantir encoding UTF-8.

## 40.5 ViewModels

Testar estados principais:

- sem permissão;
- GPS desligado;
- aguardando sinal;
- estabilizando;
- observando;
- cancelado;
- processando;
- resultado;
- erro.

---

# 41. Dados simulados para desenvolvimento

Criar um `FakeGnssRepository` para testes e previews.

Gerar sessões simuladas com:

- posição estável;
- posição ruidosa;
- outliers;
- sinal degradado;
- perda temporária do GNSS.

Isso permitirá testar a interface sem precisar permanecer fisicamente em campo.

---

# 42. Segurança e integridade

- não executar código remoto;
- não armazenar secrets no repositório;
- não incluir keystore real;
- fornecer `keystore.properties.example`;
- validar nomes de arquivos;
- sanitizar entrada CSV;
- limitar tamanho de imports;
- tratar arquivos inválidos sem crash.

---

# 43. Git e GitHub

Inicializar projeto Git caso necessário.

Branch principal:

`main`

Adicionar:

```text
.gitignore
.gitattributes
.editorconfig
```

Criar templates:

```text
.github/ISSUE_TEMPLATE/bug_report.yml
.github/ISSUE_TEMPLATE/feature_request.yml
.github/pull_request_template.md
```

Adicionar Dependabot se apropriado.

---

# 44. Licença MIT

Adicionar `LICENSE` com licença MIT.

Copyright sugerido:

```text
Copyright (c) 2026 Datum Point contributors
```

O código original do Datum Point deve ser distribuído sob MIT.

Dependências continuam sob suas próprias licenças.

Criar:

`THIRD_PARTY_NOTICES.md`

Listar:

- biblioteca;
- versão;
- licença;
- finalidade.

Não afirmar que bibliotecas Apache, BSD ou dados EPSG são MIT. Diferenciar claramente licença do projeto e licenças de terceiros.

---

# 45. Documentação

Criar:

## README.md

Incluir:

- o que é;
- screenshots futuramente;
- recursos;
- limitações;
- privacidade;
- como compilar;
- como instalar;
- como usar;
- sistemas de coordenadas;
- formatos de exportação;
- licença;
- contribuição;
- roadmap.

## docs/GNSS_PROCESSING.md

Explicar:

- pipeline;
- warm-up;
- filtros;
- outliers;
- média;
- estatísticas;
- limitações;
- por que dispersão não equivale a precisão absoluta.

## docs/COORDINATE_SYSTEMS.md

Explicar:

- WGS84;
- SIRGAS 2000;
- UTM;
- zona 22S;
- EPSG:31982;
- limitações da transformação prática.

## docs/EXPORT_FORMATS.md

Especificação de cada arquivo.

## docs/DECISIONS.md

Registrar ADRs simplificados.

## docs/TESTING.md

Como executar testes.

---

# 46. CI — GitHub Actions

Criar workflow de Pull Request / push que execute:

1. checkout;
2. configuração JDK;
3. cache Gradle;
4. lint;
5. análise estática;
6. unit tests;
7. build debug APK.

Salvar APK como artifact do workflow.

Criar também workflow para tags:

```text
v*
```

Objetivo:

- build release;
- executar testes;
- gerar artifacts;
- criar checksums SHA-256.

Não armazenar keystore no Git.

Se secrets de assinatura estiverem configurados, permitir assinatura por CI.

Se não estiverem:

- gerar artefato não assinado quando aplicável;
- documentar assinatura;
- manter debug APK facilmente instalável para testes.

---

# 47. Versionamento

Usar Semantic Versioning.

Primeira versão funcional:

**v1.0.0**

Durante implementação:

**0.x**

Criar `CHANGELOG.md`.

---

# 48. Performance

O aplicativo deve permanecer responsivo durante coleta.

Não executar processamento pesado na main thread.

Usar coroutines.

Evitar persistir dezenas de milhares de objetos Room desnecessariamente durante cada segundo de observação.

Para logs avançados grandes, preferir streaming para arquivo.

---

# 49. Consumo de bateria

O GNSS consome energia.

Requisitos:

- ativar atualizações GNSS apenas quando necessário;
- encerrar callbacks corretamente;
- não manter GNSS permanentemente ativo;
- cancelar listeners em lifecycle apropriado;
- foreground service apenas durante ocupação.

---

# 50. Tratamento de erros

Criar mensagens claras para:

- localização desativada;
- permissão negada;
- permissão “aproximada” em vez de precisa;
- GNSS indisponível;
- poucos satélites;
- sinal fraco;
- nenhuma amostra aceita;
- falha de transformação;
- falha de exportação;
- CSV inválido;
- armazenamento indisponível.

Nunca crashar por falta de GNSS bruto.

---

# 51. Cenário de aceitação principal

O seguinte fluxo deve funcionar em um aparelho real:

1. instalar APK;
2. abrir Datum Point;
3. criar projeto “Teste”;
4. CRS padrão = SIRGAS 2000 / UTM 22S;
5. tocar “Ocupar novo ponto”;
6. nome automático P01;
7. conceder localização precisa;
8. aguardar estabilização;
9. observar por 60 s;
10. visualizar dados GNSS em tempo real;
11. processar amostras;
12. visualizar latitude/longitude;
13. visualizar E/N;
14. visualizar estatísticas;
15. salvar;
16. repetir P02, P03 e P04;
17. visualizar croqui;
18. obter lados;
19. obter azimutes;
20. obter perímetro;
21. obter área;
22. cadastrar distância de referência;
23. visualizar diferença;
24. exportar CSV;
25. exportar KML;
26. exportar DXF;
27. exportar PDF;
28. compartilhar arquivo.

---

# 52. Critérios de aceite técnicos

Antes de considerar v1.0 pronta:

```text
[ ] Projeto compila do zero
[ ] Gradle Wrapper incluído
[ ] Debug APK gerado
[ ] Sem secrets versionados
[ ] Testes unitários passam
[ ] Lint passa ou possui justificativas documentadas
[ ] App funciona sem internet
[ ] Captura usa GNSS real
[ ] Permissão de localização tratada corretamente
[ ] GPS desligado tratado
[ ] GNSS bruto é opcional
[ ] Múltiplos fixes são coletados
[ ] Filtro de accuracy funciona
[ ] Outliers são processados
[ ] Estatísticas são salvas
[ ] WGS84 é preservado
[ ] UTM 22S funciona
[ ] Pontos são persistidos
[ ] Croqui funciona
[ ] Distância funciona
[ ] Azimute funciona
[ ] Área funciona
[ ] Perímetro funciona
[ ] Comparação funciona
[ ] CSV funciona
[ ] KML funciona
[ ] GeoJSON funciona
[ ] DXF funciona
[ ] TXT funciona
[ ] PDF funciona
[ ] Exportação usa APIs modernas
[ ] README existe
[ ] LICENSE MIT existe
[ ] THIRD_PARTY_NOTICES existe
[ ] CI existe
[ ] Limitações estão claramente documentadas
```

---

# 53. Plano de implementação autônomo

Este plano deve ser executado sem solicitar aprovação entre as fases.

## Fase 0 — Bootstrap

- criar estrutura do projeto;
- configurar Gradle;
- configurar Kotlin/Compose;
- criar package;
- configurar Git;
- adicionar MIT;
- adicionar arquivos básicos do repositório;
- criar tema;
- criar navegação básica;
- validar primeira compilação.

**Gate:** projeto compila.

## Fase 1 — Domínio e persistência

- criar entidades;
- Room;
- DAOs;
- repositories;
- DataStore;
- models;
- migrations;
- testes básicos.

**Gate:** CRUD de projeto e ponto passa em testes.

## Fase 2 — Motor GNSS

- permissões;
- LocationManager;
- GPS provider;
- GnssStatus;
- raw measurement feature detection;
- estado do receptor;
- coleta de fixes;
- FakeGnssRepository.

**Gate:** tela de diagnóstico recebe GNSS em aparelho/emulador quando possível e fake funciona em testes.

## Fase 3 — Processamento

- amostras;
- filtros;
- transformação métrica;
- MAD;
- outliers;
- média;
- estatísticas;
- qualidade;
- documentação matemática;
- testes.

**Gate:** suíte estatística e geodésica passa.

## Fase 4 — Ocupação

- fluxo warm-up;
- cronômetro;
- sessão;
- UI;
- progresso;
- cancelamento;
- resultado;
- persistência;
- foreground service se necessário.

**Gate:** ocupação completa P01 funciona.

## Fase 5 — Projetos e pontos

- lista de projetos;
- detalhes;
- nomeação automática;
- detalhes do ponto;
- histórico;
- edição;
- exclusão;
- reordenação.

**Gate:** usuário cria projeto e registra múltiplos pontos.

## Fase 6 — Geometria

- croqui;
- distâncias;
- azimutes;
- área;
- perímetro;
- testes;
- tela de resumo.

**Gate:** polígono de 4 pontos gera valores corretamente.

## Fase 7 — Comparação

- segmentos de referência;
- área/perímetro;
- CSV de coordenadas de referência;
- pareamento;
- deltas;
- avisos de incerteza.

**Gate:** relatório de comparação funciona.

## Fase 8 — Exportadores

Implementar e testar:

- CSV;
- TXT;
- GeoJSON;
- KML;
- KMZ;
- DXF;
- PDF.

**Gate:** arquivos são gerados e podem ser abertos externamente.

## Fase 9 — GNSS avançado

- raw measurements;
- C/N0;
- constelações;
- frequências;
- logs;
- NMEA opcional;
- export diagnóstico.

**Gate:** ausência de suporte não quebra o app.

## Fase 10 — UX, acessibilidade e robustez

- dark mode;
- mensagens;
- estados vazios;
- erros;
- acessibilidade;
- rotação;
- lifecycle;
- performance;
- bateria;
- testes em tamanhos diferentes.

## Fase 11 — Open source / CI

- documentação;
- LICENSE;
- THIRD_PARTY_NOTICES;
- CONTRIBUTING;
- SECURITY;
- GitHub Actions;
- issue templates;
- changelog;
- build artifacts.

**Gate:** CI verde.

## Fase 12 — Release candidate

Executar:

```text
./gradlew clean
./gradlew test
./gradlew lint
./gradlew assembleDebug
```

Executar também tarefas adicionais de análise estática configuradas.

Corrigir erros encontrados.

Gerar APK final de teste.

Revisar checklist de aceite.

Atualizar versão/changelog.

---

# 54. Não fazer

Não:

- usar localização única instantânea como resultado final;
- misturar Wi-Fi/network location silenciosamente;
- prometer precisão centimétrica;
- chamar dispersão de precisão absoluta;
- depender de internet;
- exigir login;
- criar backend sem necessidade;
- adicionar Firebase por conveniência;
- adicionar analytics;
- adicionar anúncios;
- adicionar trackers;
- armazenar localização em nuvem;
- usar SharedPreferences quando DataStore atende melhor;
- bloquear o desenvolvimento esperando aprovação em cada fase;
- encerrar o trabalho após produzir somente arquitetura ou mockups;
- deixar TODOs essenciais no lugar de funções necessárias à v1.0.

---

# 55. Melhorias futuras — não bloquear v1.0

Registrar no roadmap, sem atrasar a primeira versão:

- integração com receptor GNSS Bluetooth/NMEA;
- suporte RTK/NTRIP por hardware externo;
- importação DXF;
- importação KML;
- mapa com MapLibre;
- tiles offline;
- ortofotos;
- ocupação estática longa;
- RINEX;
- pós-processamento PPK;
- solução própria com raw measurements;
- correções GNSS;
- geóide;
- altitude ortométrica;
- ajuste de poligonal;
- transformação Helmert;
- melhor ajuste de geometria não georreferenciada;
- comparação automática com projeto DXF;
- sincronização opcional;
- versão desktop/web integrada ao Datumap/Geotrix.

Esses itens devem permanecer desacoplados da arquitetura principal.

---

# 56. Resultado esperado do agente

Não entregue apenas código parcial.

Ao final, deixar:

1. repositório organizado;
2. código-fonte completo;
3. licença MIT;
4. documentação;
5. testes;
6. CI;
7. APK debug instalável;
8. build release preparado;
9. exportadores;
10. comparação;
11. GNSS básico funcionando;
12. suporte diagnóstico GNSS bruto;
13. lista objetiva de limitações reais.

Na resposta final, informar:

```text
STATUS DO DATUM POINT

Build:
Testes:
Lint:
APK:
Versão:
Funcionalidades concluídas:
Limitações conhecidas:
Arquivos/documentos importantes:
Próximos itens opcionais:
```

Se algum item não puder ser concluído por limitação concreta do ambiente, deixar o restante funcional e explicar exatamente:

- o que faltou;
- por que faltou;
- qual comando ou ação resolve.

---

# 57. Filosofia final

O objetivo do Datum Point não é fingir que um smartphone é um receptor geodésico.

O objetivo é transformar o GNSS já existente no aparelho em uma ferramenta de campo **mais transparente, repetível, documentada e útil** do que simplesmente abrir um aplicativo de coordenadas e copiar uma leitura instantânea.

Cada ponto deve carregar consigo não apenas uma coordenada, mas também informações suficientes para responder:

- quanto tempo foi observado;
- quantas leituras foram utilizadas;
- quanto elas se dispersaram;
- qual qualidade o Android reportou;
- quais condições GNSS estavam presentes;
- qual metodologia gerou aquela coordenada.

**Precisão deve ser tratada como dado, não como aparência de casas decimais.**
