# Minha Bateria

Aplicativo Android em Kotlin para acompanhar dados de bateria e carregamento localmente.

## Versão

- versionName: 1.0.5
- versionCode: 5
- versão completa: 1.0.5+6
- package: `com.minhabateria.app`

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
- tela Agora compacta e sem rolagem;
- Configurações separadas da tela principal;
- seção Sobre com versão e alterações recentes;
- botão Doação que copia a chave Pix para a área de transferência.

## Interface

A tela Agora mantém apenas as informações essenciais, sem rolagem, com dimensões responsivas para alturas de tela menores. O conteúdo respeita as áreas da barra de status e da navegação do Android, mantém medidor e status agrupados e usa o espaço excedente abaixo das informações em vez de separar artificialmente os elementos. Valores indisponíveis usam cor neutra e as abas futuras permanecem legíveis sem aparentar erro.

As abas Gráficos, Sessão e Histórico continuam reservadas para as próximas etapas e não possuem lógica fictícia nesta versão.

## Monitoramento contínuo

O foreground service continua sendo iniciado somente quando solicitado pelo usuário. No Android 13 ou superior, o app solicita permissão para notificações. A opção de retomar após reiniciar permanece opcional e depende da preferência salva pelo usuário.

O app não utiliza `WAKE_LOCK` e não mantém a tela ligada artificialmente.

## Assinatura e atualização

A partir desta versão, o workflow gera um APK `release` assinado com uma chave permanente. Os dados da chave não ficam no repositório: são fornecidos por GitHub Secrets.

O arquivo `Minha-Bateria-GitHub-Secrets.txt` é entregue separadamente do ZIP do código. Guarde esse arquivo com segurança. As próximas versões devem usar os mesmos Secrets para que o Android reconheça a assinatura e permita instalar a atualização por cima da versão instalada.

Consulte `SIGNING.md`.

## Arquitetura

O código é dividido por responsabilidade em `battery`, `calculation`, `monitoring`, `settings`, `ui` e `utils`.
Nenhum arquivo de código deve ultrapassar 500 linhas.

## Build release

Requisitos: JDK 17, Android SDK 35 e os quatro Secrets de assinatura configurados no ambiente/GitHub.

O workflow executa:

```bash
gradle :app:assembleRelease
```

Artefato final do workflow:

`Minha-Bateria-1.0.5.apk`

## Observação de medição

`BATTERY_PROPERTY_CURRENT_NOW` depende do suporte do fabricante e representa a corrente observada na bateria pelo sistema, não uma medição direta da saída do painel, carregador ou power bank. Quando o aparelho não fornece uma leitura válida, o aplicativo mostra `Indisponível`.

## Distribuição no GitHub

O APK é publicado diretamente em GitHub Releases como `Minha-Bateria-1.0.5.apk`. O código-fonte é separado em `Minha-Bateria-1.0.5-source.zip`. O workflow não usa `actions/upload-artifact` para o APK.
