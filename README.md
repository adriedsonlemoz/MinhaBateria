# Minha Bateria

Aplicativo Android simples em Kotlin para acompanhar o carregamento do celular.

## Versão

- versionName: 1.0.1
- versionCode: 2
- versão completa: 1.0.1+2
- package: `com.minhabateria.app`

## Recursos atuais

- porcentagem da bateria;
- carregando / não carregando;
- fonte detectada: tomada, USB, sem fio ou bateria;
- tensão;
- corrente quando o aparelho disponibiliza a leitura;
- potência calculada a partir de tensão e corrente;
- temperatura da bateria;
- tempo conectado na sessão atual;
- pico de corrente e potência da sessão;
- monitoramento contínuo por foreground service;
- notificação permanente com dados resumidos;
- controles para iniciar e parar o monitoramento;
- opção de retomar o monitoramento após reiniciar o aparelho.

## Monitoramento contínuo

O serviço só é iniciado quando solicitado pelo usuário. No Android 13 ou superior, o app solicita permissão para notificações. O foreground service continua podendo ser iniciado se a permissão for negada, sujeito ao comportamento do Android para notificações de serviços em primeiro plano.

A opção `Retomar após reiniciar` usa `BOOT_COMPLETED` e somente tenta restaurar o serviço se o monitoramento estava ativo antes da reinicialização.

O app não usa `WAKE_LOCK`. A tela também não é mantida artificialmente ligada, evitando alterar desnecessariamente o consumo observado durante os testes.

## Arquitetura

O código é dividido por responsabilidade em `battery`, `calculation`, `monitoring`, `ui` e `utils`.
Nenhum arquivo Kotlin deve ultrapassar 500 linhas.

## Build

Requisitos: JDK 17 e Android SDK 35.

No Works ou em ambiente Android configurado:

```bash
gradle :app:assembleDebug
```

APK gerado originalmente em:

`app/build/outputs/apk/debug/app-debug.apk`

O workflow incluído copia o artefato com o nome:

`Minha-Bateria-1.0.1.apk`

## Observação de medição

`BATTERY_PROPERTY_CURRENT_NOW` depende do suporte do fabricante e representa a corrente observada na bateria pelo sistema, não uma medição direta da saída do painel, carregador ou power bank. Alguns aparelhos podem não fornecer corrente válida. Nesses casos, o aplicativo mostra `Indisponível` e não inventa um valor.
