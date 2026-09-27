# Validação — Minha Bateria 1.0.33+34

Data: 2026-09-27

## Escopo desta versão

- redesenho da tela Perfil da fonte para preenchimento guiado por toques;
- presets e seletores específicos para carregador, power bank, painel solar e outra fonte;
- seleção rápida de marca, modelo/preset, potência, protocolo, porta e saída;
- seleção adicional de capacidade em power bank e de cabo em carregador;
- seleção adicional de tensão, corrente e controlador/conversor em painel solar;
- preset do painel solar de 8 W com os valores suportados pela etiqueta fornecida: 8 W, 5 V e 1,6 A;
- prévia dinâmica do perfil antes de salvar;
- campos manuais mantidos somente como alternativa para etiquetas que não correspondam aos presets.

## Regras de confiabilidade preservadas

- os valores 8 W, 5 V e 1,6 A do preset solar vêm da etiqueta fornecida;
- controlador/conversor não é preenchido automaticamente pelo preset de 8 W porque essa informação não aparece na etiqueta analisada;
- presets genéricos não atribuem marca/modelo específico sem escolha do usuário;
- dados da etiqueta continuam tratados como referência nominal e não como medição direta da saída;
- correções anteriores de monitoramento, descarga, gráficos, Sessão e Android 16 permanecem intactas.

## Sincronização

- `versionName 1.0.33` e `versionCode 34` sincronizados entre `VERSION`, Gradle, `app_identity.json`, README, Works e workflow;
- tela de novidades atualizada para 1.0.33+34 e continua exibida uma única vez por versão;
- workflow configurado para publicar somente `Minha-Bateria-1.0.33.apk`.

## Verificações

- XMLs parseados pelo validador do projeto;
- referências de IDs, drawables e cores verificadas;
- Manifest/launcher verificados;
- limite de 500 linhas por arquivo de código verificado;
- ausência de APK, AAB, keystore, secrets e diretórios de build/cache dentro do source verificada;
- build Android completo depende de ambiente com Gradle/Android SDK e só deve ser marcado como concluído após execução efetiva.
