# Validação — Minha Bateria 1.0.27+28

Data: 2026-09-26

## Escopo desta versão

- refinamento visual da tela Consumo de bateria;
- barras proporcionais para participação dos apps na atividade em primeiro plano;
- porcentagem e duração apresentadas separadamente;
- avisos de interpretação e privacidade reorganizados;
- placeholders de design limitados a `tools:` e não usados em execução.

## Regras de confiabilidade preservadas

- o ranking continua baseado somente em eventos reais de uso fornecidos pelo Android;
- a porcentagem continua representando participação no tempo em primeiro plano observado, não consumo elétrico por aplicativo;
- nenhuma potência, corrente, descarga ou autonomia é inferida a partir do ranking de apps;
- corrente bruta continua preservando o sinal fornecido pelo Android;
- quando dados reais não existem, a interface mantém `—`, estado de cálculo ou mensagem explicativa.

## Sincronização

- `versionName 1.0.27` e `versionCode 28` sincronizados entre `VERSION`, Gradle, `app_identity.json`, README, Works e workflow;
- tela de novidades atualizada para 1.0.27+28 e continua exibida uma única vez por versão;
- workflow configurado para publicar somente `Minha-Bateria-1.0.27.apk`.

## Verificações

- XMLs parseados pelo validador do projeto;
- referências de IDs, drawables e cores verificadas;
- Manifest/launcher verificados;
- limite de 500 linhas por arquivo de código verificado;
- ausência de APK, AAB, keystore, secrets e diretórios de build/cache dentro do source verificada;
- build Android completo depende de ambiente com Gradle/Android SDK e não é assumido como executado quando essas ferramentas não estão disponíveis.
