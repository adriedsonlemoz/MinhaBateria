# Minha Bateria

Aplicativo Android simples em Kotlin para acompanhar o carregamento do celular.

## Versão

- versionName: 1.0.0
- versionCode: 1
- versão completa: 1.0.0+1
- package: `com.minhabateria.app`

## Recursos atuais

- porcentagem da bateria;
- carregando / não carregando;
- fonte de energia: tomada, USB, sem fio ou bateria;
- tensão;
- corrente de carga quando o aparelho disponibiliza a leitura;
- potência aproximada calculada a partir de tensão e corrente;
- temperatura da bateria;
- tempo conectado na sessão atual;
- pico de corrente e potência da sessão.

## Arquitetura

O código é dividido por responsabilidade em `battery`, `calculation`, `ui` e `utils`.
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

`Minha-Bateria-1.0.0.apk`

## Observação de medição

`BATTERY_PROPERTY_CURRENT_NOW` depende do suporte do fabricante. Alguns aparelhos podem não fornecer corrente válida. Nesses casos, o aplicativo mostra `Indisponível` e não inventa um valor.
