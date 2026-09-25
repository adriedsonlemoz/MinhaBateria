# Minha Bateria

Aplicativo Android nativo em Kotlin para acompanhar dados de bateria e carregamento localmente.

## Versão

- versionName: 1.0.6
- versionCode: 7
- versão completa: 1.0.6+7
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
- seção Sobre com versão e alterações recentes;
- botão Doação que copia a chave Pix para a área de transferência.

## Interface 1.0.6

A tela Agora foi redesenhada com fundo azul-profundo, cards em camadas, bordas discretas, medidor circular com destaque luminoso, métricas com ícones próprios e navegação inferior com ícones. O layout continua sem rolagem e usa dimensões responsivas para acomodar telas menores.

As abas Gráficos, Sessão e Histórico continuam reservadas para etapas futuras e não possuem lógica fictícia.

## Monitoramento contínuo

O foreground service é iniciado somente quando solicitado pelo usuário. No Android 13 ou superior, o app solicita permissão para notificações. A retomada após reiniciar é opcional.

O app não utiliza `WAKE_LOCK` e não mantém a tela ligada artificialmente.

## Assinatura e atualização

O APK de distribuição é `release` e usa a chave permanente introduzida na versão 1.0.3+4. Os dados da chave são fornecidos por GitHub Secrets e não ficam no ZIP do código-fonte.

Use sempre o mesmo arquivo `Minha-Bateria-GitHub-Secrets.txt` para permitir atualização por cima da instalação existente.

## Arquitetura

O código é dividido por responsabilidade em `battery`, `calculation`, `monitoring`, `settings`, `ui` e `utils`. Nenhum arquivo de código deve ultrapassar 500 linhas.

## Build release

Requisitos: JDK 17, Android SDK 35 e os quatro Secrets de assinatura configurados.

```bash
gradle :app:assembleRelease
```

APK final: `Minha-Bateria-1.0.6.apk`

## Observação de medição

`BATTERY_PROPERTY_CURRENT_NOW` depende do suporte do fabricante e representa a corrente observada na bateria pelo sistema, não uma medição direta da saída do painel, carregador ou power bank. Quando o aparelho não fornece uma leitura válida, o aplicativo mostra `Indisponível`.

## Distribuição no GitHub

O APK é publicado diretamente em GitHub Releases como `Minha-Bateria-1.0.6.apk`. O código-fonte é publicado separadamente como `Minha-Bateria-1.0.6-source.zip`. O workflow não usa `actions/upload-artifact` para o APK.
