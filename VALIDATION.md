# Validação — Minha Bateria 1.0.36+37

Data: 2026-09-27

## Escopo desta versão

- compactação da tela Perfil da fonte;
- seletores inteligentes de tipo, marca e modelo;
- preset X-TRAD SH-106 (8 W / 5 V / 1,6 A);
- migração do armazenamento de um perfil para vários perfis sem perder o perfil legado;
- eventos de energia do Android e seleção da fonte física usada na sessão;
- tolerância de 15 segundos para reconexões rápidas;
- exclusão segura no Histórico e em Comparar cargas;
- revisão tipográfica em Agora, Gráficos, Sessão, Histórico, Comparar cargas e Perfil da fonte.

## Armazenamento e exclusão

O histórico atual é persistido em `SharedPreferences` como uma lista JSON de `HistoryEntry`. Cada entrada contém os agregados da sessão (Wh, mAh, médias, mínimos/máximos, temperatura, porcentagens e interrupções) e não possui tabela/coleção filha.

O buffer usado pelos gráficos é um histórico global recente de até 60 minutos e **não possui `sessionId` nem propriedade exclusiva de uma sessão**. Por isso, excluir uma sessão remove somente o `HistoryEntry` selecionado; não é correto apagar o buffer global por intervalo de tempo, pois isso poderia remover leituras que também representam o monitoramento geral. Não ficam referências órfãs de comparação, pois as telas resolvem as sessões novamente pelo ID e removem/encerram seleções inválidas.

## Conexão e fonte física

- `ACTION_POWER_CONNECTED` e `ACTION_POWER_DISCONNECTED` alimentam o estado de conexão;
- a leitura periódica de `BatteryStatusReader` funciona como fallback caso um broadcast não seja observado;
- AC/USB/sem fio são tratados como **tipo de conexão informado pelo Android**, não como identificação da fonte física;
- o perfil físico só é associado à nova sessão quando o usuário escolhe um perfil; “Agora não” não interrompe a sessão e não confirma silenciosamente a fonte anterior;
- reconexões dentro de 15 s preservam a sessão e não reabrem a seleção.

## Sincronização

- `versionName 1.0.36` e `versionCode 37` sincronizados entre `VERSION`, Gradle, `app_identity.json`, README, Works e workflow;
- tela de novidades atualizada para 1.0.36+37;
- workflow configurado para publicar `Minha-Bateria-1.0.36.apk` na release `v1.0.36`.

## Verificações executadas

- `python3 tools/validate_project.py`: **OK** — versão 1.0.36+37, 124 XMLs válidos, recursos/IDs/launcher consistentes e source limpo;
- compilação Kotlin isolada de `ChargingSession`, `SessionAccumulator`, `SourcePresetCatalog` e dependências puras: **OK**;
- smoke test: desconexão curta não encerra a sessão; reconexão preserva a sessão e contabiliza interrupção; desconexão acima da tolerância encerra no instante inicial da perda de energia: **OK**;
- smoke test do preset X-TRAD SH-106 e opções de entrada manual: **OK**;
- build Android release local: **não executado neste ambiente**, pois não há Android SDK/Gradle Wrapper configurados. O workflow permanece preparado para o build release assinado.
