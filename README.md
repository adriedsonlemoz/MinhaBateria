# Minha Bateria

Aplicativo Android nativo em Kotlin para acompanhar dados de bateria e carregamento localmente.

## Versão

- versionName: 1.0.7
- versionCode: 8
- versão completa: 1.0.7+8
- package: `com.minhabateria.app`

## Base técnica

- Kotlin 2.0.21;
- Android Gradle Plugin 8.7.3;
- Java/JVM 17;
- compileSdk 35;
- targetSdk 35;
- minSdk 26;
- interface XML tradicional, sem Jetpack Compose;
- build de produção `release` assinado.

## Recursos atuais

- porcentagem da bateria;
- carregando / não carregando;
- fonte detectada pelo Android;
- perfil configurável da fonte usada no teste;
- tipos: painel solar, carregador, power bank e outra fonte;
- nome personalizado e potência nominal opcional, obrigatória para painel solar;
- tensão;
- corrente quando o dispositivo disponibiliza a leitura;
- potência calculada;
- temperatura da bateria;
- tempo conectado;
- pico de corrente e potência;
- monitoramento contínuo por foreground service;
- notificação permanente;
- retomada opcional após reiniciar;
- Configurações separadas da tela principal;
- seção Sobre e botão Doação.

## Perfil da fonte

Na primeira abertura sem perfil configurado, o app oferece a configuração uma vez. O usuário pode escolher `Agora não` e configurar posteriormente em Configurações.

O perfil informado pelo usuário é mantido separado da conexão detectada pelo Android. Exemplo:

- perfil: `Painel 8W`;
- detectado pelo Android: `USB`.

O perfil é salvo localmente e não precisa ser preenchido novamente a cada abertura.

## Interface

A tela Agora usa identidade visual azul-profundo, cards em camadas, medidor circular, métricas com ícones e navegação inferior. O layout principal continua sem rolagem.

As abas Gráficos, Sessão e Histórico permanecem reservadas para etapas futuras e não possuem lógica fictícia.

## Monitoramento contínuo

O foreground service é iniciado somente quando solicitado pelo usuário. No Android 13 ou superior, o app solicita permissão para notificações. A retomada após reiniciar é opcional.

O app não utiliza `WAKE_LOCK` e não mantém a tela ligada artificialmente.

## Assinatura e atualização

O APK de distribuição é `release` e usa a chave permanente introduzida na versão 1.0.3+4. Os dados da chave são fornecidos por GitHub Secrets e não ficam no ZIP do código-fonte.

Use sempre o mesmo arquivo `Minha-Bateria-GitHub-Secrets.txt` para permitir atualização por cima da instalação existente.

## Arquitetura

O código é dividido por responsabilidade em `battery`, `calculation`, `monitoring`, `settings`, `source`, `ui` e `utils`. Nenhum arquivo de código deve ultrapassar 500 linhas.

## Build release

Requisitos: JDK 17, Android SDK 35 e os quatro Secrets de assinatura configurados.

```bash
gradle :app:assembleRelease
```

APK final: `Minha-Bateria-1.0.7.apk`

## Observação de medição

`BATTERY_PROPERTY_CURRENT_NOW` depende do suporte do fabricante e representa a corrente observada na bateria pelo sistema, não uma medição direta da saída do painel, carregador ou power bank. Quando o aparelho não fornece uma leitura válida, o aplicativo mostra `Indisponível`.

## Distribuição no GitHub / Works

O workflow gera e publica **somente** `Minha-Bateria-1.0.7.apk` como arquivo de entrega. Ele não cria nem publica um source ZIP e não usa `actions/upload-artifact` para o APK.

O ZIP de código-fonte usado para desenvolvimento é mantido fora do fluxo de entrega do Works/GitHub Actions.
