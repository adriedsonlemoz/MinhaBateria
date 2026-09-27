# Validação — Minha Bateria 1.0.34+35

Data: 2026-09-27

## Escopo desta versão

- reforma completa de UX/UI em Agora, Perfil da fonte, Sessão e Gráficos;
- camada reutilizável leigo → números úteis → técnico;
- resumo humano central da situação da carga com critérios determinísticos;
- estimativas com estado explícito de “ainda coletando” quando a amostra é insuficiente;
- perfil de fonte com seletores como método principal e dados técnicos recolhidos por padrão;
- resumo de sessão com progresso, ritmo, potência média, energia e temperatura;
- número de amostras mantido no painel técnico;
- gráficos com interpretação contextual, período compacto, detalhes sob demanda e melhor referência de zero;
- escala adaptativa de bateria com amplitude mínima de 12 pontos percentuais;
- exibição de temperatura sem casas decimais artificiais quando o dado recebido é inteiro;
- documentação dos critérios em `INTERPRETATION.md`.

## Regras de confiabilidade preservadas

- não foram adicionadas novas medições inexistentes no Android;
- perfil/etiqueta continua sendo dado configurado, separado da conexão detectada pelo sistema;
- potência é cálculo a partir das leituras do aparelho e não medição direta da saída elétrica total da fonte;
- comparação com potência nominal é contexto, não eficiência;
- mensagens de possível problema usam linguagem probabilística e não diagnosticam defeito físico;
- estimativas de tempo só aparecem quando há fonte válida de estimativa;
- corrente preserva o sinal bruto reportado pelo Android;
- dados técnicos existentes continuam disponíveis em áreas expansíveis.

## Sincronização

- `versionName 1.0.34` e `versionCode 35` sincronizados entre `VERSION`, Gradle, `app_identity.json`, README, Works e workflow;
- tela de novidades atualizada para 1.0.34+35 e continua exibida uma única vez por versão;
- workflow configurado para publicar somente `Minha-Bateria-1.0.34.apk` na release `v1.0.34`.

## Verificações

- `python3 tools/validate_project.py`: **OK** — versão 1.0.34+35, 121 XMLs válidos, recursos/IDs/launcher consistentes e source limpo;
- maior arquivo Kotlin/Java validado: `MetricChartView.kt`, 399 linhas (limite do projeto: 500);
- maior XML validado: `activity_discharge_rate.xml`, 473 linhas;
- verificação adicional dos bindings por tela: **OK** para Agora, Gráficos, Sessão e Perfil da fonte, incluindo layouts incluídos;
- compilação isolada da camada Kotlin sem Android (`kotlinc`): **OK** para sessão, acumulador, interpretadores, métricas e formatadores alterados;
- smoke tests de lógica: **OK** para carga lenta, carga muito instável, temperatura crítica, comparação nominal, poucas amostras, corrente positiva/negativa e estabilidade da bateria;
- build Android release local: **não executado neste ambiente**, pois não há Gradle nem Android SDK instalados e o projeto não inclui Gradle Wrapper. O workflow do repositório continua configurado para Gradle 8.9, Android build release e assinatura pelos Secrets permanentes.
