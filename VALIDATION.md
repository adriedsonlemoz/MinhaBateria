# Validação — Minha Bateria 1.0.30+31

Data: 2026-09-26

## Escopo desta versão

- redesenho visual da tela Descarga baseado no mockup aprovado;
- novo `DischargeTrendView` desenhado nativamente para representar o progresso entre percentual inicial e atual;
- cálculo e apresentação do horário aproximado de término usando a autonomia já validada pelo estimador;
- nova comparação textual entre taxa atual e média histórica;
- reorganização dos quatro indicadores principais e do histórico de descarga;
- atualização visual do botão de reinício e dos cards de autonomia e leitura rápida.

## Regras de confiabilidade preservadas

- autonomia e horário previsto continuam indisponíveis até existir a amostra mínima de 3 min e 1% de queda real;
- o gráfico superior representa somente a tendência entre início e estado atual, sem afirmar que barras intermediárias são medições reais;
- a comparação com histórico só aparece quando existem descargas concluídas válidas;
- nenhum valor ausente é substituído por zero ou estimativa fictícia;
- correções anteriores do monitoramento contínuo e do Android 16 permanecem intactas.

## Sincronização

- `versionName 1.0.30` e `versionCode 31` sincronizados entre `VERSION`, Gradle, `app_identity.json`, README, Works e workflow;
- tela de novidades atualizada para 1.0.30+31 e continua exibida uma única vez por versão;
- workflow configurado para publicar somente `Minha-Bateria-1.0.30.apk`.

## Verificações

- XMLs parseados pelo validador do projeto;
- referências de IDs, drawables e cores verificadas;
- Manifest/launcher verificados;
- limite de 500 linhas por arquivo de código verificado;
- ausência de APK, AAB, keystore, secrets e diretórios de build/cache dentro do source verificada;
- build Android completo depende de ambiente com Gradle/Android SDK e só deve ser marcado como concluído após execução efetiva.
