# Validação — Minha Bateria 1.0.35+36

Data: 2026-09-27

## Escopo desta versão

- reforma completa da tela Comparar cargas baseada no mockup fornecido;
- cartões de Sessão A/B com fonte, data e tipo;
- resumo humano do maior resultado observado;
- seis métricas rápidas com barras relativas e diferenças contextualizadas;
- explicação automática que diferencia duração, potência média e total acumulado;
- painel técnico recolhido por padrão;
- exclusão individual de sessões do Histórico com confirmação;
- remoção automática da seleção quando uma sessão selecionada é excluída.

## Regras de confiabilidade preservadas

- nenhuma medição nova foi inventada;
- energia e carga continuam sendo estimativas calculadas a partir dos dados observados no aparelho;
- barras da comparação são relativas somente entre A e B dentro da mesma métrica;
- o resumo não chama o maior total de maior eficiência;
- ausência de dados produz estado indisponível, sem preencher valores por suposição;
- exclusão afeta somente a sessão escolhida e exige confirmação explícita.

## Sincronização

- `versionName 1.0.35` e `versionCode 36` sincronizados entre `VERSION`, Gradle, `app_identity.json`, README, Works e workflow;
- tela de novidades atualizada para 1.0.35+36 e continua exibida uma única vez por versão;
- workflow configurado para publicar somente `Minha-Bateria-1.0.35.apk` na release `v1.0.35`.

## Verificações

- `python3 tools/validate_project.py`: **OK** — versão 1.0.35+36, 124 XMLs válidos, recursos/IDs/launcher consistentes e source limpo;
- compilação isolada da lógica Kotlin da comparação (`HistoryComparisonFormatter` + dependências): **OK**;
- smoke tests do resumo A/B: **OK** para maior carga na Sessão A, maior potência média na Sessão B, diferença de energia, barras relativas, painel rápido/técnico e potência nominal no título;
- build Android release local: **não executado neste ambiente**, pois Gradle, ADB/AAPT2 e Android SDK não estão instalados; o workflow permanece configurado para Gradle 8.9, build release e assinatura permanente.
