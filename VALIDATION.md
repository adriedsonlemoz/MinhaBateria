# Validação — Minha Bateria 1.0.21+22

Data: 2026-09-25

## Alterações desta etapa

- indicador verde de carregamento passa a mostrar diretamente a previsão até 100%;
- removido o texto genérico `Carregando` quando há previsão disponível;
- card `Consumo de bateria` adicionado ao espaço inferior da tela Agora;
- nova tela de consumo com taxa de descarga, corrente instantânea e ranking de apps mais ativos nas últimas 6 horas;
- fluxo de `Acesso ao uso` integrado para consultar estatísticas de atividade dos aplicativos;
- ranking descrito explicitamente como indicador de atividade, não como consumo elétrico exato por app;
- Configurações, pop-up de novidades, documentação e workflow atualizados.

## Validações executadas

- `versionName 1.0.21` e `versionCode 22` sincronizados entre `VERSION`, Gradle, `app_identity.json`, README e workflow;
- XMLs analisados e bem formados;
- IDs e resources usados pelo Kotlin validados;
- nova activity declarada no Manifest e permissão `PACKAGE_USAGE_STATS` presente;
- source sem APK, AAB, keystore, Secrets, `build` ou `.gradle`;
- nenhum arquivo de código acima de 500 linhas;
- Works configurado para publicar somente `Minha-Bateria-1.0.21.apk`.

## Build Android

O ambiente desta entrega não possui Gradle instalado para executar a compilação Android local. O workflow permanece preparado para executar a validação e o build `release` assinado no runner do GitHub Actions.
