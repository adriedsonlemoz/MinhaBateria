# Validação — Minha Bateria 1.0.28+29

Data: 2026-09-26

## Escopo desta versão

- redesenho visual da tela Gráficos;
- uma métrica grande por vez com seletores de Bateria, Corrente, Potência e Temperatura;
- janelas de 5, 15 e 60 minutos mantidas;
- gráfico ampliado com mínimo/máximo, grade, eixo de tempo, preenchimento e marcador da amostra mais recente;
- seleção de métrica e intervalo preservada durante recriações da Activity;
- escala de bateria fixada em 0–100% e referência em zero para corrente/potência quando aplicável.

## Regras de confiabilidade preservadas

- o gráfico usa somente amostras registradas pelo monitoramento existente;
- nenhum valor é criado para preencher pontos ausentes;
- amostras nulas e intervalos acima de 30 segundos interrompem o traçado, evitando ligar visualmente períodos sem observação;
- corrente mantém o sinal bruto recebido do Android;
- o preenchimento sob a linha é apenas representação visual da mesma série, sem suavização ou geração de pontos intermediários;
- quando não há pelo menos duas amostras válidas, a tela informa que está aguardando amostras.

## Sincronização

- `versionName 1.0.28` e `versionCode 29` sincronizados entre `VERSION`, Gradle, `app_identity.json`, README, Works e workflow;
- tela de novidades atualizada para 1.0.28+29 e continua exibida uma única vez por versão;
- workflow configurado para publicar somente `Minha-Bateria-1.0.28.apk`.

## Verificações

- XMLs parseados pelo validador do projeto;
- referências de IDs, drawables e cores verificadas;
- Manifest/launcher verificados;
- limite de 500 linhas por arquivo de código verificado;
- ausência de APK, AAB, keystore, secrets e diretórios de build/cache dentro do source verificada;
- build Android completo depende de ambiente com Gradle/Android SDK e não é assumido como executado quando essas ferramentas não estão disponíveis.
