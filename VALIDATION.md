# Validação final — Minha Bateria 1.0.17+18

Data: 2026-09-25

## Validado neste ambiente

- sincronização de `versionName 1.0.17` e `versionCode 18` entre `VERSION`, Gradle, `app_identity.json`, README e workflow;
- XMLs do Manifest e recursos bem formados;
- referências Kotlin a IDs, layouts, drawables e cores existentes;
- launcher com `@string/app_name` e intent MAIN/LAUNCHER;
- nenhum arquivo Kotlin/Python acima de 500 linhas;
- source sem APK, AAB, keystore, Secrets, diretórios `build` ou `.gradle`;
- workflow publica somente `Minha-Bateria-1.0.17.apk` como arquivo de entrega;
- teste Kotlin do ciclo de sessão: conectar, integrar, pausar, retomar, atingir 100%, congelar totais, desconectar e iniciar nova sessão;
- teste Kotlin de lacuna superior a 15 segundos, confirmando que energia e tempo de carga não são extrapolados;
- teste Kotlin do buffer de gráficos, incluindo intervalo mínimo entre amostras e descarte por idade;
- teste Kotlin da comparação de Histórico com 14 métricas e diferença B − A;
- tela Diagnóstico adicionada e ligada a Configurações/Manifest;
- validação automatizada adicionada ao Works antes da reconstrução do keystore e antes do build.

## Correções encontradas durante a revisão

- o README anterior mostrava `versionCode: 16` enquanto a versão real 1.0.16 usava `versionCode 17`; a documentação foi sincronizada e o novo validador passa a bloquear esse tipo de divergência;
- ao parar manualmente o monitoramento contínuo, o estado persistido da sessão podia permanecer salvo e ser retomado em um início posterior do serviço. O encerramento agora limpa explicitamente a sessão persistida quando o monitoramento é desativado.

## Limitação deste ambiente

O ambiente de criação possui Java e Kotlin, mas não possui Android SDK/Gradle configurado. Por isso não foi possível executar localmente `gradle :app:assembleRelease` nem instalar o APK em um dispositivo/emulador.

O workflow executa `python3 tools/validate_project.py` e depois `gradle :app:assembleRelease` em runner com Java 17 e Gradle 8.9. A publicação do Release só acontece se essas etapas concluírem com sucesso.
