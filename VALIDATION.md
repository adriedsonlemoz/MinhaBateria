# Validação — Minha Bateria 1.0.25+26

Data: 2026-09-26

## Alterações desta etapa

- primeira etapa do refinamento visual concentrada na tela Agora;
- contornos e superfícies dos cards tornados mais discretos para reduzir excesso de azul;
- fonte atual e perfil configurado reorganizados para não parecerem informações contraditórias;
- métricas sem leitura passam a usar `—` e ficam visualmente atenuadas;
- origem das medições ganhou etiqueta compacta, mantendo a informação técnica sem competir com o valor principal;
- resumo de três colunas ficou contextual: carga mostra Tempo/Energia/Carga; descarga mostra Tempo na bateria/Queda/Média;
- navegação inferior recebeu seleção ativa mais compacta e tipografia padronizada;
- status de carregamento ganhou tratamento verde escuro mais equilibrado no tema escuro;
- nenhum cálculo novo foi criado para preencher dados ausentes: o resumo de descarga usa somente `ActiveDischarge` e `BatteryRateEstimator` já existentes.

## Validações executadas

- `versionName 1.0.25` e `versionCode 26` sincronizados entre `VERSION`, Gradle, `app_identity.json`, README, Works e workflow;
- XMLs analisados e bem formados;
- IDs e resources usados pelo Kotlin validados;
- source sem APK, AAB, keystore, Secrets, `build` ou `.gradle`;
- nenhum arquivo de código acima de 500 linhas;
- revisão dos estados conectado, carregando, carga pausada e fora da tomada na renderização da tela Agora;
- Works configurado para publicar somente `Minha-Bateria-1.0.25.apk`.

## Build Android

O ambiente desta entrega não possui Gradle/Android SDK configurado para executar a compilação Android local. O workflow permanece preparado para executar a validação e o build `release` assinado no runner do GitHub Actions.
