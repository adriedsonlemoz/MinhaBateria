# Validação — Minha Bateria 1.0.38+39

Data: 2026-09-27

## Escopo desta versão

- integração dos 24 avisos OGG fornecidos;
- configuração global e individual de voz, volume e segundo plano;
- áudio personalizado via Storage Access Framework com permissão persistente e fallback para o padrão;
- fila única de reprodução, prioridades, cooldown e controle por transição de estado;
- gerenciamento independente da seleção usada para comparação;
- seleção múltipla, exclusão em lote e exclusão total do histórico;
- diálogos internos personalizados para ações destrutivas e fonte conectada.

## Persistência e segurança

As preferências de áudio ficam em `SharedPreferences` separado e não alteram medições, sessões nem histórico existentes. Desativar o controle geral não apaga as escolhas individuais. URIs personalizadas usam permissão persistente de leitura; se o arquivo não estiver mais acessível ou falhar na reprodução, o vínculo é removido e o OGG padrão volta a ser usado automaticamente.

O histórico continua persistido em `SharedPreferences` como JSON de `HistoryEntry`. A exclusão em lote regrava a coleção sem os IDs selecionados e usa `commit()` para confirmar a gravação antes de atualizar a interface. A sessão atualmente em andamento permanece no `ContinuousSessionStore` e não faz parte da coleção de histórico até ser concluída, portanto a limpeza do histórico não a apaga.

Comparação e gerenciamento usam conjuntos de IDs diferentes. Após qualquer exclusão, ambos são filtrados contra os IDs que ainda existem, evitando referências quebradas.

## Áudio e segundo plano

Os avisos são coordenados pelo `BatteryMonitorService` já existente. Não foi criado serviço paralelo. Enquanto o monitoramento contínuo estiver ativo, o foreground service pode continuar processando os eventos em segundo plano conforme as limitações normais do Android. O usuário pode impedir voz fora do primeiro plano sem desligar o monitoramento.

Prioridade da fila: temperatura crítica, bateria em 5%, bateria em 10%, problemas de carregamento e avisos informativos. A fila possui limite curto e nunca inicia dois `MediaPlayer` ao mesmo tempo.

## Sincronização

- `versionName 1.0.38` e `versionCode 39` sincronizados entre `VERSION`, Gradle, `app_identity.json`, README, Works e workflow;
- tela de novidades atualizada para 1.0.38+39;
- workflow configurado para publicar `Minha-Bateria-1.0.38.apk` na release `v1.0.38`.

## Verificações executadas

- `python3 tools/validate_project.py`: **OK** — versão, XMLs, recursos/IDs/launcher e source limpo;
- 24 arquivos OGG: **OK** — todos reconhecidos como Vorbis, conteúdo único e mapeamento 1:1 em `VoiceAlertEvent`;
- smoke test Kotlin isolado do coordenador de voz: **OK** — conexão inicial, início de sessão, temperatura crítica/normalização, bateria 20%, carga lenta sem repetição contínua, carga normalizada e oscilação sem repetição;
- separação estrutural de seleção no Histórico: **OK** — `compareSelectedIds` e `manageSelectedIds` independentes;
- build Android release local: **não executado neste ambiente**, pois não há Android SDK/Gradle instalado. O workflow permanece preparado para o build release assinado.
