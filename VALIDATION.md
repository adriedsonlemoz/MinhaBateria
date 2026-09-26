# Validação — Minha Bateria 1.0.23+24

Data: 2026-09-26

## Alterações desta etapa

- notificação persistente transformada em resumo vivo, sem preencher a linha principal com `Indisponível`;
- descarga mostra `Calculando autonomia…` até existir amostra mínima e depois apresenta autonomia aproximada e ritmo observado;
- carregamento mostra previsão até 100% quando disponível e mantém estado próprio para carga completa ou pausada;
- notificação expandida inclui resumo da sessão e corrente instantânea quando disponível;
- corrente instantânea da tela Consumo de bateria passa a usar sinal visual: negativa/vermelha na descarga e positiva/verde na carga;
- leitura de corrente foi isolada em componente próprio para normalizar a direção exibida sem alterar o acumulador das sessões de carregamento;
- documentação, novidades e workflow atualizados para a nova versão.

## Validações executadas

- `versionName 1.0.23` e `versionCode 24` sincronizados entre `VERSION`, Gradle, `app_identity.json`, README e workflow;
- XMLs analisados e bem formados;
- IDs e resources usados pelo Kotlin validados;
- source sem APK, AAB, keystore, Secrets, `build` ou `.gradle`;
- nenhum arquivo de código acima de 500 linhas;
- Works configurado para publicar somente `Minha-Bateria-1.0.23.apk`.

## Build Android

O ambiente desta entrega não possui Gradle/Android SDK configurado para executar a compilação Android local. O workflow permanece preparado para executar a validação e o build `release` assinado no runner do GitHub Actions.
