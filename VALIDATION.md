# Validação — Minha Bateria 1.0.24+25

Data: 2026-09-26

## Alterações desta etapa

- corrente instantânea mantém o sinal bruto informado pelo Android, sem `abs()` ou inversão baseada no estado de carga;
- potência de carga só usa corrente positiva válida enquanto o sistema informa carregamento;
- autonomia de descarga e taxa em `%/h` só aparecem após pelo menos 3 minutos e 1% de queda real;
- tela Agora recebeu autonomia no status, `Velocidade de descarga` dinâmica e corrente de descarga com sinal/cor;
- regra de taxa/autonomia centralizada entre Agora, Descarga, Consumo de bateria e notificação;
- participação dos apps não força mais mínimo artificial de 1% e o texto explicita `atividade observada`;
- placeholders numéricos fictícios removidos dos layouts dinâmicos;
- documentação, novidades e workflow atualizados para a nova versão.

## Validações executadas

- `versionName 1.0.24` e `versionCode 25` sincronizados entre `VERSION`, Gradle, `app_identity.json`, README e workflow;
- XMLs analisados e bem formados;
- IDs e resources usados pelo Kotlin validados;
- source sem APK, AAB, keystore, Secrets, `build` ou `.gradle`;
- nenhum arquivo de código acima de 500 linhas;
- varredura por `Random`, mocks, dados demo e placeholders numéricos de bateria;
- subconjunto Kotlin sem dependências Android compilado com `kotlinc`, incluindo estimador de taxa/autonomia e formatadores;
- teste do estimador confirmou bloqueio antes de 3 min e cálculo correto após amostra válida;
- Works configurado para publicar somente `Minha-Bateria-1.0.24.apk`.

## Build Android

O ambiente desta entrega não possui Gradle/Android SDK configurado para executar a compilação Android local. O workflow permanece preparado para executar a validação e o build `release` assinado no runner do GitHub Actions.
