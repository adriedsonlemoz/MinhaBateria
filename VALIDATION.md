# Validação — Minha Bateria 1.0.26+27

Data: 2026-09-26

## Escopo desta versão

- refinamento visual do medidor circular da tela Agora;
- autonomia/tempo até 100% integrados ao medidor sem gerar dados artificiais;
- estado abaixo do medidor simplificado para evitar repetição;
- acessibilidade do medidor e do estado atualizada;
- remoção de placeholder antigo de versão em Configurações.

## Regras de confiabilidade preservadas

- autonomia de descarga continua dependendo de no mínimo 3 minutos e 1% de queda real observada;
- previsão de carga prioriza a estimativa do Android e usa fallback da sessão somente quando a amostra mínima já é válida;
- quando não há amostra suficiente, a interface mostra estado de cálculo em vez de criar um tempo;
- corrente bruta continua preservando o sinal fornecido pelo Android;
- campos ausentes continuam sem valores fabricados.

## Sincronização

- `versionName 1.0.26` e `versionCode 27` sincronizados entre `VERSION`, Gradle, `app_identity.json`, README, Works e workflow;
- tela de novidades atualizada para 1.0.26+27 e continua exibida uma única vez por versão;
- workflow configurado para publicar somente `Minha-Bateria-1.0.26.apk`.

## Verificações

- XMLs parseados pelo validador do projeto;
- referências de IDs, drawables e cores verificadas;
- Manifest/launcher verificados;
- limite de 500 linhas por arquivo de código verificado;
- ausência de APK, AAB, keystore, secrets e diretórios de build/cache dentro do source verificada;
- build Android completo depende de ambiente com Gradle/Android SDK e não é assumido como executado quando essas ferramentas não estão disponíveis.
