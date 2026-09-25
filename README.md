# Minha Bateria

Aplicativo Android nativo em Kotlin para acompanhar dados de bateria e carregamento localmente.

## Versão

- versionName: 1.0.10
- versionCode: 11
- versão completa: 1.0.10+11
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

- porcentagem e estado da bateria;
- fonte detectada pelo Android;
- tensão, corrente, potência calculada e temperatura;
- tempo da sessão, Wh/mAh estimados e picos observados;
- monitoramento contínuo por foreground service;
- perfil técnico da fonte usada no teste;
- Configurações, Sobre e Doação;
- build release assinado e publicação direta do APK.

## Perfil técnico da fonte

O perfil não exige mais um nome inventado. O usuário escolhe o tipo da fonte e copia os dados que conseguir ler na etiqueta. O nome pode ser gerado automaticamente, por exemplo `Samsung EP-TA800 • 25 W`.

### Carregador

Pode registrar marca, modelo, potência máxima declarada, saídas da etiqueta, tecnologia/protocolo, porta usada e informação do cabo.

### Power bank

Pode registrar marca, modelo, capacidade em mAh, potência máxima, saídas, protocolo e porta.

### Painel solar

Registra potência nominal e pode guardar tensão/corrente da etiqueta e o controlador ou conversor usado. A potência nominal continua obrigatória para painel solar.

### Outra fonte

Aceita os dados conhecidos sem exigir especificações inexistentes.

Os dados de etiqueta são referência nominal e permanecem separados da conexão detectada pelo Android. Eles não são tratados como medição direta da energia entregue ao aparelho.

## Origem e confiabilidade dos dados

- **Sistema:** valores fornecidos pelo Android quando disponíveis;
- **Calculado:** valores derivados das leituras, como potência;
- **Estimado:** Wh, mAh e médias temporais integradas somente em intervalos com leituras válidas.

Valores ausentes permanecem como `Indisponível`.

## Motor da sessão

A sessão começa na primeira amostra confirmada como carregando e mantém duração total, tempo efetivamente carregando, interrupções, bateria inicial/atual, ganho percentual, Wh e mAh estimados, médias temporais, mínimos, máximos e temperatura média/máxima.

Wh e mAh são integrados entre amostras válidas ao longo do tempo. Intervalos acima de 15 segundos, leituras ausentes, períodos sem carregamento e mudança da fonte detectada não são integrados, evitando extrapolações falsas. O monitoramento contínuo persiste o estado da sessão para recuperação após reinício do serviço; uma reinicialização completa do aparelho inicia uma nova sessão.

## Interface

A tela Agora mantém a identidade azul-profundo, sem rolagem vertical, com medidor circular, cards compactos e navegação inferior. Configurações e formulários auxiliares podem rolar quando necessário.

## Monitoramento contínuo

O foreground service é iniciado somente quando solicitado. O app não utiliza `WAKE_LOCK`. A retomada após reiniciar é opcional.

## Assinatura e atualização

O APK usa a chave release permanente introduzida na versão 1.0.3+4. Use sempre o mesmo `Minha-Bateria-GitHub-Secrets.txt` para instalar atualizações por cima da versão existente.

## Arquitetura

O código é dividido por responsabilidade em módulos. Nenhum arquivo de código deve ultrapassar 500 linhas.

## Build release

```bash
gradle :app:assembleRelease
```

APK final: `Minha-Bateria-1.0.10.apk`

## Distribuição no GitHub / Works

O workflow publica somente `Minha-Bateria-1.0.10.apk` como arquivo de entrega. Não usa `actions/upload-artifact` para o APK e não publica source ZIP como saída do Works.
