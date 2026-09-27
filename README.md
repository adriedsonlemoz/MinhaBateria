# Minha Bateria

Aplicativo Android nativo em Kotlin para acompanhar dados de bateria e carregamento localmente.

## Versão

- versionName: 1.0.34
- versionCode: 35
- versão completa: 1.0.34+35
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
- tensão, corrente bruta com sinal, velocidade de carga e velocidade de descarga observada, além de temperatura;
- tempo da sessão, Wh/mAh estimados, picos observados e estimativa de tempo até 100% quando disponível;
- monitoramento contínuo por foreground service;
- perfil técnico da fonte usada no teste com preenchimento rápido por sugestões;
- navegação principal com Agora, Gráficos, Sessão, Descarga e Histórico;
- tela Descarga com consumo em %/h, autonomia estimada, projeção da bateria em 1 hora, qualidade da amostra, média histórica e histórico próprio;
- módulo Consumo de bateria com taxa atual, corrente instantânea com direção visual (+ entrada / − descarga) e ranking de apps mais ativos nas últimas 6 horas mediante Acesso ao uso;
- gráficos de bateria, corrente, potência e temperatura em visualização ampla, com novo cabeçalho de valor atual, resumo rápido, eixo lateral e seleção de 5, 15 e 60 minutos;
- diagnóstico técnico copiável/exportável com estado do monitoramento, leituras, sessão, histórico, gráficos e falhas capturadas;
- capturador global de exceções fatais com até 10 relatórios locais, incluindo stack trace, tela aberta, versão, aparelho, memória e estado da bateria;
- Configurações, Sobre e Doação;
- build release assinado e publicação direta do APK.

## Diagnóstico e falhas

O app instala um capturador global de exceções fatais logo na inicialização. Quando uma falha Java/Kotlin não tratada encerra o processo, o Minha Bateria tenta salvar um relatório antes de delegar o encerramento normal ao Android.

- os relatórios ficam somente no armazenamento interno privado do aplicativo;
- são mantidos no máximo os 10 mais recentes;
- o relatório inclui versão, tela ativa, thread, exceção, stack trace, Android, modelo, memória, armazenamento e estado recente da bateria;
- na próxima abertura, o app avisa uma única vez que o fechamento anterior foi registrado;
- em Configurações > Diagnóstico e erros é possível copiar, exportar em `.txt` ou limpar as falhas;
- não há upload automático, SDK externo de analytics ou envio de telemetria para servidor.

O capturador é voltado a exceções fatais do código Java/Kotlin. Encerramentos forçados pelo sistema, falhas nativas e alguns ANRs podem não produzir um relatório completo.

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

### Preenchimento rápido

O formulário foi redesenhado para priorizar toques em vez de digitação. O usuário escolhe primeiro o tipo da fonte e depois usa seletores/presets específicos. Carregadores oferecem marca, modelo/preset, potência, protocolo, porta, saída e cabo; power banks acrescentam capacidade; painéis solares acrescentam tensão, corrente e controlador/conversor. Os campos manuais continuam disponíveis apenas como ajuste para etiquetas diferentes.

O catálogo inclui um preset baseado na etiqueta analisada do painel solar de 8 W: **8 W, 5 V e 1,6 A**. Selecionar esse preset preenche esses campos de uma vez, sem transformar os valores em medição do Android.

O formulário oferece seletores de marca, modelo/preset, potência, protocolo, porta e saídas comuns. Entre as opções de protocolo estão USB-PD, USB-PD 3.0, USB-PD 3.1, PPS e combinações comuns. Para Samsung há presets de modelos conhecidos como EP-TA800 25 W e EP-T4510 45 W; para painéis e power banks há presets por potência/capacidade. Digitação manual fica como alternativa quando a etiqueta não corresponder às sugestões.

### Comparação com a referência nominal

Quando o perfil possui potência nominal, a tela Agora mostra quanto da referência configurada está sendo observado no aparelho. A aba Sessão contextualiza o pico da sessão da mesma forma. Essa comparação não é apresentada como eficiência nem como medição direta da saída da fonte.

## Origem e confiabilidade dos dados

- **Sistema:** valores fornecidos pelo Android quando disponíveis;
- **Calculado:** valores derivados das leituras, como potência;
- **Estimado:** Wh, mAh e médias temporais integradas somente em intervalos com leituras válidas.

Valores ausentes continuam identificados nas telas técnicas quando necessário. Na notificação persistente, campos sem leitura são omitidos para evitar textos como `Indisponível`.

## Motor da sessão

A sessão começa quando uma fonte física é conectada e zera os contadores da sessão anterior. Ao atingir 100%, os totais são congelados como carga completa; ao desconectar a fonte, a sessão atual é encerrada. Duração total e tempo efetivamente carregando permanecem separados.

Wh e mAh são integrados entre amostras válidas ao longo do tempo. Intervalos acima de 15 segundos, leituras ausentes, períodos sem carregamento e mudança da fonte detectada não são integrados, evitando extrapolações falsas. O monitoramento contínuo persiste o estado da sessão para recuperação após reinício do serviço; uma reinicialização completa do aparelho inicia uma nova sessão.

## Interface

A versão 1.0.34 aplica uma camada única de leitura **leigo → números úteis → técnico** nas telas Agora, Perfil da fonte, Sessão e Gráficos. A informação principal é sempre o estado humano (por exemplo `Carga normal`, `Carga lenta`, `Carga oscilando` ou `Aparelho quente`); os números essenciais aparecem logo depois e os dados técnicos ficam recolhidos em **Ver detalhes técnicos**.

### Agora

O painel principal prioriza porcentagem, estado da bateria e previsão aproximada. A estimativa de 100% só aparece quando o Android fornece uma previsão válida ou quando a sessão já possui amostra mínima suficiente; caso contrário a interface informa que a estimativa ainda está sendo preparada. O perfil configurado da fonte fica separado do tipo de conexão detectado pelo Android e o app não inventa `Tomada (AC)` quando o sistema não identificou a conexão.

A situação da carga é interpretada com critérios documentados e baseada em temperatura, sinal da corrente, tempo da sessão, oscilação de potência e, quando configurada, referência nominal da fonte. Durante a carga, os quatro indicadores rápidos são **Potência agora**, **Corrente agora**, **Temperatura** e **Energia recebida**; fora da fonte, os mesmos espaços reaproveitam o contexto para mostrar **Ritmo de descarga** e **Queda da bateria**, preservando o acompanhamento de descarga sem criar mais cards. Tensão, mAh e demais dados técnicos permanecem disponíveis sob demanda.

### Perfil da fonte

O cadastro começa perguntando o que fornece energia: **Painel solar, Carregador, Power bank ou Outra fonte**. Marca, modelo/preset, potência e capacidade quando aplicável usam seletores como caminho principal. Campos de etiqueta, protocolos, portas, cabo, controlador e conversor ficam em **Dados técnicos da etiqueta**, recolhidos por padrão e filtrados conforme o tipo selecionado.

O resumo **Seu perfil** mostra exatamente o que foi informado antes de salvar e conta quantos dados técnicos opcionais foram configurados. Especificações ausentes permanecem ausentes; o aplicativo não completa valores por suposição.

### Sessão

O topo responde rapidamente se a sessão está em andamento, pausada ou concluída, mostra a variação real da bateria e, quando já existe tempo suficiente, o ritmo aproximado em `%/h`. O resumo mantém bateria, tempo, energia recebida, potência média e temperatura máxima.

O diagnóstico humano reutiliza os mesmos critérios da tela Agora e usa linguagem de possibilidade para causas não comprovadas. A referência nominal configurada é comparada apenas com o que foi observado no aparelho, deixando explícito que isso **não é medição direta da saída elétrica total da fonte**. Corrente/tensão média e faixa, pico, Wh, mAh, número de amostras, interrupções e demais métricas continuam no painel técnico recolhido.

### Gráficos

Bateria, Corrente, Potência e Temperatura usam o mesmo cabeçalho: valor atual, estado interpretado, contexto e explicação em linguagem natural. O período usa um seletor compacto de 5, 15 ou 60 minutos. Estatísticas de mínimo, máximo, média e número de amostras ficam recolhidas por padrão.

A escala da bateria é adaptativa, mas mantém uma faixa mínima de 12 pontos percentuais para evitar transformar pequenas oscilações em mudanças visualmente enormes. Corrente positiva/negativa preserva o sinal bruto do Android e, quando cruza zero, o gráfico destaca a linha de zero. Temperatura respeita a resolução observada: valores inteiros não ganham uma casa decimal artificial. Lacunas de coleta continuam como lacunas, sem virar zero nem ser ligadas artificialmente.

Os critérios de interpretação estão documentados em [`INTERPRETATION.md`](INTERPRETATION.md).

## Ciclo automático da sessão

- conectar uma fonte inicia uma nova sessão em `00:00:00`;
- pausas de carregamento com o cabo ainda conectado contam como interrupções, sem encerrar a sessão;
- ao atingir 100%, duração e acumulados são congelados para preservar o resultado final;
- desconectar a fonte encerra a sessão atual;
- a próxima conexão sempre inicia uma sessão nova;
- o Histórico salva automaticamente a sessão concluída quando a fonte é desconectada, sem botão manual de salvar;
- são mantidas até 100 sessões recentes para consulta e futura comparação.

## Resumo inteligente

A aba Sessão contextualiza potência atual, média e pico, compara o valor observado com a referência nominal configurada e classifica a estabilidade pelas oscilações das amostras válidas. Essas classificações são interpretações do comportamento observado no aparelho, não medições científicas da fonte.

## Gráficos

Os gráficos armazenam no máximo uma amostra a cada 10 segundos, embora a leitura principal possa continuar em frequência maior. São mantidos até 60 minutos de dados recentes, com seleção de janelas de 5, 15 e 60 minutos. A persistência é feita de forma espaçada para reduzir escritas em armazenamento e consumo do próprio aplicativo. Leituras indisponíveis aparecem como lacunas, sem serem convertidas em zero.

## Monitoramento contínuo

O foreground service é iniciado somente quando solicitado. O app não utiliza `WAKE_LOCK`. A retomada após reiniciar é opcional. A notificação persistente funciona como resumo vivo: durante a descarga mostra autonomia estimada e ritmo apenas após pelo menos 3 minutos e 1% de queda real; durante a carga mostra previsão até 100%; ao expandir, inclui dados úteis da sessão e corrente instantânea quando disponíveis, sem preencher a linha com campos `Indisponível`.

## Diagnóstico e validação

Em Configurações, a tela **Diagnóstico** reúne versão, aparelho/Android, estado do monitoramento contínuo, permissão de notificações, perfil da fonte, leitura atual, resumo da sessão, quantidade de sessões do Histórico e amostras dos Gráficos. O relatório pode ser atualizado e copiado para facilitar análise de problemas, sem incluir keystore, Secrets ou chaves de assinatura.

O projeto inclui `tools/validate_project.py`. O Works executa essa validação antes do build release e bloqueia a publicação se encontrar versão dessincronizada, XML inválido, recurso/ID ausente, launcher incorreto, artefatos proibidos no source ou arquivo de código acima de 500 linhas.

Durante a revisão final também foi corrigido o encerramento manual do monitoramento contínuo: ao parar o serviço, o estado persistido da sessão ativa é limpo para impedir que uma sessão antiga seja retomada indevidamente quando o monitoramento for iniciado novamente.

## Assinatura e atualização

O APK usa a chave release permanente introduzida na versão 1.0.3+4. Use sempre o mesmo `Minha-Bateria-GitHub-Secrets.txt` para instalar atualizações por cima da versão existente.

## Arquitetura

O código é dividido por responsabilidade em módulos. Nenhum arquivo de código deve ultrapassar 500 linhas.

## Build release

```bash
gradle :app:assembleRelease
```

APK final: `Minha-Bateria-1.0.34.apk`

## Distribuição no GitHub / Works

O workflow publica somente `Minha-Bateria-1.0.34.apk` como arquivo de entrega. Não usa `actions/upload-artifact` para o APK e não publica source ZIP como saída do Works.


## Comparação de sessões

O Histórico permite selecionar duas sessões para comparar lado a lado os valores observados e estimados, sem escolher automaticamente uma sessão vencedora.

## APK atual

O workflow publica diretamente `Minha-Bateria-1.0.34.apk` na GitHub Release `v1.0.34`.
