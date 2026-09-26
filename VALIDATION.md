# Validação — Minha Bateria 1.0.19+20

Data: 2026-09-25

## Alterações desta etapa

- estimativa de tempo até 100% com origem identificável: previsão do Android quando disponível ou fallback conservador baseado na sessão;
- tela Agora com linguagem simplificada e ajuda para W, mA, Wh e mAh;
- aba Sessão com resumo simples e detalhes técnicos recolhidos;
- status `Carga completa` ao atingir 100%.

## Validações exigidas

- `versionName 1.0.19` e `versionCode 20` sincronizados entre `VERSION`, Gradle, `app_identity.json`, README e workflow;
- XMLs bem formados e IDs/resources Kotlin existentes;
- estimador não gera previsão antes de dados mínimos da sessão;
- estimativa do Android tem prioridade quando válida;
- source sem APK, AAB, keystore, Secrets, `build` ou `.gradle`;
- nenhum arquivo de código acima de 500 linhas;
- Works publica somente `Minha-Bateria-1.0.19.apk`.

## Build Android

O ambiente de geração desta entrega não possui Android SDK/Gradle configurado para executar `assembleRelease` localmente. O workflow executa validação e build release em runner preparado.
