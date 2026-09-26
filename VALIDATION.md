# Validação — Minha Bateria 1.0.20+21

Data: 2026-09-25

## Alterações desta etapa

- módulo Descarga promovido para a navegação inferior principal;
- tela Descarga ampliada com projeção da bateria em 1 hora, média histórica e indicador de qualidade da amostra;
- pop-up de novidades controlado por versão, exibido apenas uma vez após cada atualização;
- atalho secundário de Descarga mantido em Configurações;
- documentação e workflow sincronizados com a nova versão.

## Validações executadas

- `versionName 1.0.20` e `versionCode 21` sincronizados entre `VERSION`, Gradle, `app_identity.json`, README e workflow;
- 81 XMLs analisados e bem formados;
- IDs e resources usados pelo Kotlin validados;
- navegação inferior contém Agora, Gráficos, Sessão, Descarga e Histórico;
- source sem APK, AAB, keystore, Secrets, `build` ou `.gradle`;
- nenhum arquivo de código acima de 500 linhas;
- Works configurado para publicar somente `Minha-Bateria-1.0.20.apk`.

## Build Android

O ambiente desta entrega não possui Gradle instalado para executar a compilação Android local. O workflow permanece preparado para executar a validação e o build `release` assinado no runner do GitHub Actions.
