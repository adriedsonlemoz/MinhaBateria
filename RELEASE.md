# Release — Minha Bateria 1.0.29+30

## Destaques

- correção do fechamento no Android 16 durante restauração automática do monitoramento contínuo;
- tratamento seguro de `ForegroundServiceStartNotAllowedException` no serviço e no controlador;
- restauração `START_STICKY` diferencia inicialização automática de solicitação explícita do usuário;
- recusa temporária do Android não é mais convertida em crash fatal;
- diagnóstico registra a última recusa de inicialização do serviço;
- permissão de notificações negada não dispara mais o monitoramento por engano.

## Entrega

APK esperado pelo workflow: `Minha-Bateria-1.0.29.apk`.

O ZIP de código-fonte não inclui APK, keystore, secrets nem diretórios de build/cache.
