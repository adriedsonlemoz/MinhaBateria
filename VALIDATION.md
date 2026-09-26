# Validação — Minha Bateria 1.0.29+30

Data: 2026-09-26

## Escopo desta versão

- correção do crash `ForegroundServiceStartNotAllowedException` observado no Android 16;
- tratamento explícito de recriação `START_STICKY` com `intent == null`;
- proteção no `BatteryMonitorService.startForeground()` e em `MonitoringServiceController.start()`;
- preservação da intenção de monitoramento para nova tentativa ao voltar ao primeiro plano;
- registro não fatal da última recusa de inicialização no Diagnóstico;
- início após `POST_NOTIFICATIONS` somente quando a permissão foi realmente concedida.

## Regras de confiabilidade preservadas

- uma recusa do Android para iniciar/promover o foreground service não encerra mais o processo do aplicativo;
- uma restauração automática só prossegue se o monitoramento continuar solicitado;
- quando a promoção para foreground falha, o serviço encerra aquela execução com `START_NOT_STICKY`;
- falhas desconhecidas continuam sendo propagadas, evitando esconder bugs não relacionados à restrição de foreground service;
- o registro da recusa é separado dos relatórios de crash fatal.

## Sincronização

- `versionName 1.0.29` e `versionCode 30` sincronizados entre `VERSION`, Gradle, `app_identity.json`, README, Works e workflow;
- tela de novidades atualizada para 1.0.29+30 e continua exibida uma única vez por versão;
- workflow configurado para publicar somente `Minha-Bateria-1.0.29.apk`.

## Verificações

- XMLs parseados pelo validador do projeto;
- referências de IDs, drawables e cores verificadas;
- Manifest/launcher verificados;
- limite de 500 linhas por arquivo de código verificado;
- ausência de APK, AAB, keystore, secrets e diretórios de build/cache dentro do source verificada;
- build Android completo depende de ambiente com Gradle/Android SDK e só deve ser marcado como concluído após execução efetiva.
