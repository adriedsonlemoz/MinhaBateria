# Changelog

## 1.0.37+38

- Integrados os 24 arquivos OGG fornecidos em `res/raw`, cada um mapeado a um evento de carga, bateria, temperatura, sessão ou monitoramento, sem duplicação de áudio.
- Criada a seção **Áudio e avisos** em Configurações, com controle geral, volume, permissão de avisos em segundo plano e 24 controles individuais organizados em grupos recolhíveis.
- Cada evento permite ativar/desativar, testar, escolher arquivo de áudio pelo Storage Access Framework e restaurar o áudio padrão.
- Referências de áudios personalizados são persistidas com permissão de leitura; falhas de acesso, corrupção ou reprodução retornam automaticamente ao OGG padrão correspondente.
- O `BatteryMonitorService` existente passou a coordenar os avisos, sem serviço paralelo, com fila curta, reprodução sem sobreposição, prioridade por gravidade, cooldown e controle por transição de estado.
- Avisos de temperatura, bateria crítica e problemas de carga não repetem continuamente enquanto a mesma condição permanece ativa; normalização libera novos alertas quando apropriado.
- Histórico ganhou modos independentes **Comparar** e **Gerenciar**, preservando o limite de duas sessões apenas para comparação e usando seleção separada para exclusão.
- Adicionados toque longo para entrar em gerenciamento, selecionar todas, limpar seleção, excluir várias sessões e excluir todo o histórico, sempre com contagem real e confirmação destrutiva.
- Exclusões limpam seleções inválidas imediatamente e não alteram a sessão ativa armazenada pelo monitoramento contínuo.
- Criado componente visual reutilizável para confirmações internas; exclusão individual, múltipla, total e exclusão pela tela Comparar cargas usam o novo padrão visual.
- O diálogo **Fonte conectada** foi redesenhado com card da fonte sugerida, ações compactas e seletor de perfis com identidade visual do Minha Bateria.
- Adicionadas animações discretas para entrada de diálogos, seleção de sessões e expansão/recolhimento das categorias de áudio.
- Validação do projeto agora confere os 24 OGG, assinatura Ogg, unicidade de conteúdo e mapeamento completo em `VoiceAlertEvent`.
- Versão, identidade, README, novidades, Works, Release, Validation e workflow sincronizados para 1.0.37+38.

## 1.0.36+37

- Perfil da fonte compactado: os quatro seletores grandes foram substituídos por um único seletor de tipo de fonte.
- Marca e Modelo agora são seletores inteligentes lado a lado quando há espaço, mantendo digitação manual como alternativa.
- Adicionado preset conhecido **X-TRAD SH-106** para painel solar com 8 W, VMP 5 V e IMP 1,6 A; todos os valores continuam editáveis.
- `SourceProfileStore` passou a preservar múltiplos perfis, migrando automaticamente o perfil único das versões anteriores sem apagar dados.
- Eventos `ACTION_POWER_CONNECTED` / `ACTION_POWER_DISCONNECTED` passaram a alimentar o fluxo de seleção da fonte física, com fallback pela leitura periódica do Android.
- Ao conectar alimentação, o app oferece a última fonte usada, os perfis já cadastrados e a opção de adicionar outra fonte, sem impedir a coleta caso o usuário escolha “Agora não”.
- O app separa explicitamente a conexão detectada pelo Android da fonte física informada pelo usuário; uma nova sessão sem confirmação não herda silenciosamente um perfil antigo.
- Reconexões de até 15 segundos são tratadas como a mesma sessão, evitando várias sessões e avisos por pequenas oscilações.
- Histórico e Comparar cargas usam a mesma confirmação destrutiva antes de excluir; excluir uma sessão selecionada fecha a comparação inválida e atualiza a seleção ao retornar.
- Revisada a relação de armazenamento: o histórico guarda agregados por sessão; o buffer de gráficos é global/recente e não possui vínculo de propriedade por `sessionId`, portanto a exclusão não remove amostras globais não pertencentes exclusivamente à sessão.
- Revisão tipográfica nas telas Agora, Gráficos, Sessão, Histórico, Comparar cargas e Perfil da fonte, elevando textos auxiliares/legendas e preservando escala de fonte do Android.
- Versão, identidade, README, novidades, Works, Release, Validation e workflow sincronizados para 1.0.36+37.

## 1.0.35+36

- Tela Comparar cargas redesenhada conforme o novo mockup, com cartões A/B, resumo humano, comparação rápida e detalhes técnicos sob demanda.
- Adicionado resumo do maior resultado observado sem classificar automaticamente maior carga acumulada como maior eficiência.
- Duração, tempo carregando, energia, carga, potência média e pico agora aparecem em cards comparativos com barras relativas A/B.
- Criado card “O que isso significa” para contextualizar diferenças de duração, potência e condições da sessão.
- Corrente, tensão, temperatura, referência nominal, ganho, interrupções e estabilidade foram movidos para o painel técnico recolhido.
- Histórico ganhou lixeira por sessão com confirmação obrigatória antes da exclusão.
- Excluir uma sessão remove também sua seleção atual para impedir comparação com registro apagado.
- Tela de novidades, workflow, identidade, README, Works, Release, Validation e versão sincronizados para 1.0.35+36.

## 1.0.34+35

- Reforma completa de UX/UI nas telas Agora, Perfil da fonte, Sessão e Gráficos, preservando medições e funcionalidades existentes.
- Adotada hierarquia reutilizável de três níveis: entendimento imediato, números úteis e detalhes técnicos sob demanda.
- Tela Agora agora destaca estado da bateria, estimativa humana, fonte configurada separada da conexão detectada e diagnóstico de carga baseado em dados reais.
- Durante descarga, a tela Agora preserva ritmo em `%/h`, queda acumulada e autonomia no mesmo layout compacto, sem manter cards elétricos sem sentido.
- Criado `ChargeConditionInterpreter` para classificar carga normal, lenta, oscilando, possível perda de carga e temperatura elevada com critérios determinísticos e linguagem não conclusiva sobre hardware.
- Perfil da fonte passa a perguntar primeiro o tipo, priorizar seletores e recolher dados técnicos da etiqueta por padrão, mostrando somente campos relevantes.
- Tela Sessão ganhou progresso da bateria, ritmo aproximado, diagnóstico humano, comparação contextual com a referência nominal e número de amostras no painel técnico.
- Gráficos ganharam período compacto, interpretação em linguagem natural, detalhes técnicos recolhidos, escala de bateria com amplitude mínima segura e indicação explícita da linha zero na corrente.
- Temperatura evita falsa precisão ao omitir a casa decimal quando a leitura fornecida é inteira.
- Documentados origem dos dados, critérios de interpretação e limites em `INTERPRETATION.md`.
- Tela de novidades, workflow, identidade, README, Works, Release e versão sincronizados para 1.0.34+35.

## 1.0.33+34

- Tela Perfil da fonte redesenhada para priorizar preenchimento guiado e reduzir a necessidade de digitação manual.
- Carregador, power bank, painel solar e outra fonte agora usam presets e seletores específicos por tipo.
- Adicionados seletores para capacidade do power bank, cabo do carregador, tensão/corrente e controlador do painel solar.
- Catálogo de presets ampliado com opções genéricas de carregadores USB-C, power banks e fontes USB sem inventar marca/modelo.
- Adicionado preset de painel solar 8 W com 5 V e 1,6 A, baseado na etiqueta fornecida.
- A prévia do perfil agora resume automaticamente tipo, modelo, potência e dados relevantes antes de salvar.
- Tela de novidades, workflow, identidade, documentação e versão sincronizados para 1.0.33+34.

## 1.0.32+33

- Aplicado o novo padrão visual em toda a tela Gráficos, cobrindo Bateria, Corrente, Potência e Temperatura.
- Cada gráfico agora mostra valor atual destacado, linha de contexto, resumo rápido em quatro blocos e eixo lateral mais legível.
- O componente de gráfico foi ajustado para priorizar a área útil do traçado, manter lacunas reais e reforçar a leitura temporal.
- A tela Sessão ganhou um painel técnico redesenhado com seções visuais para tempo, energia, dados elétricos e temperatura/bateria.
- O botão de detalhes técnicos da Sessão agora alterna o estado visual entre abrir e recolher o painel.
- Tela de novidades, workflow, identidade, documentação e versão sincronizados para 1.0.32+33.

## 1.0.31+32

- Redesenhada a tela Sessão conforme o novo mockup aprovado.
- Adicionado estado visual de conexão: sem carregamento, carregando ou carga completa.
- Fonte da sessão ganhou ícone, hierarquia visual e referência nominal mais legível.
- Resumo da sessão passou a usar cards para bateria, tempo, energia recebida, velocidade média e temperatura máxima.
- Adicionado painel de orientação quando ainda não existe sessão ativa.
- Redesenhada a tela Histórico conforme o novo mockup aprovado.
- Histórico agora separa duração, energia, carga, média, pico, faixa de bateria, ganho, temperatura máxima, estabilidade e interrupções.
- Adicionados chips de estabilidade calculados a partir do `powerVariationRatio` já salvo em cada sessão.
- Fluxo de comparação ganhou contador 0/2, estado pronto e seleção visual mais clara.
- Tela de novidades, documentação, identidade, workflow e versão sincronizados para 1.0.31+32.

## 1.0.30+31

- Tela **Descarga** redesenhada com hierarquia visual mais clara, cards enriquecidos e leitura mais direta do estado atual.
- Card principal ganhou uma tendência gráfica nativa da descarga entre o percentual inicial e o atual, sem usar imagem estática ou fabricar amostras intermediárias.
- **Autonomia estimada** passa a mostrar também o horário aproximado em que a bateria pode chegar a 0%, incluindo indicação de amanhã ou data quando necessário.
- Quatro resumos foram reorganizados com ícones: percentual consumido, tempo medido, previsão para 1 hora e média histórica com quantidade de descargas salvas.
- Nova seção **Leitura rápida** compara o ritmo atual com a média histórica e resume a previsão de término, mantendo aviso sobre brilho, sinal, tela e apps em segundo plano.
- Histórico de descarga redesenhado para destacar taxa, faixa de bateria, data e duração com menor poluição visual.
- Botão de reinício recebeu destaque de ação principal e a identidade visual da tela foi aproximada do restante do tema escuro do app.
- Versão, identidade, documentação, novidades e workflow sincronizados para 1.0.30+31.

## 1.0.29+30

- Corrigido fechamento no Android 16 causado por `ForegroundServiceStartNotAllowedException` durante restauração automática do `BatteryMonitorService`.
- Recriações `START_STICKY` com `intent == null` agora verificam se o monitoramento continua solicitado e encerram com segurança quando a promoção para foreground é recusada.
- Inicialização do serviço ganhou proteção também no controlador; recusas recuperáveis deixam o monitoramento pendente para nova tentativa quando o app voltar ao primeiro plano.
- Diagnóstico passa a registrar data, origem, exceção e detalhe da última recusa ao iniciar o serviço, sem contabilizá-la como crash fatal.
- Corrigido o fluxo de `POST_NOTIFICATIONS`: o serviço só é iniciado após confirmação de permissão concedida.
- Versão, identidade, documentação, novidades e workflow sincronizados para 1.0.29+30.

## 1.0.28+29

- Tela Gráficos redesenhada: removida a grade 2×2 e adotado um gráfico amplo com seleção entre Bateria, Corrente, Potência e Temperatura.
- Mantidos os intervalos de 5, 15 e 60 minutos, com seleção preservada durante recriações da tela.
- Gráfico ganhou mínimo/máximo, grade, eixo de tempo, preenchimento discreto e marcador da leitura mais recente.
- Corrente usa vermelho quando a última leitura é negativa e verde quando positiva, preservando o sinal bruto recebido.
- Escala da bateria usa 0–100%; corrente e potência mantêm a referência em zero quando aplicável para reduzir exageros visuais.
- Lacunas de leitura e intervalos maiores que 30 segundos não são conectados artificialmente no traçado.
- Versão, identidade, documentação, novidades e workflow sincronizados para 1.0.28+29.

## 1.0.27+28

- Tela Consumo de bateria refinada visualmente com hierarquia mais clara no resumo de descarga, avisos compactos e melhor leitura no tema escuro.
- Ranking de aplicativos substitui selos genéricos por barras proporcionais de atividade observada, porcentagem destacada e tempo em primeiro plano separado.
- Texto da seção reforça que a porcentagem representa participação no tempo em primeiro plano das últimas 6 horas, não consumo elétrico medido por aplicativo.
- Placeholders visuais do ranking usam apenas `tools:` para prévia de layout; em execução os dados continuam vindo exclusivamente do Android.
- Fluxo de permissão de Acesso ao uso recebeu apresentação mais clara e explicação de privacidade.
- Versão, identidade, documentação, novidades e workflow sincronizados para 1.0.27+28.

## 1.0.26+27

- Segunda etapa do refinamento visual da tela Agora aplicada ao medidor circular e à área de autonomia.
- Medidor passa a exibir percentual e previsão no mesmo componente: autonomia restante na descarga ou tempo até 100% durante a carga.
- Nenhuma previsão é inventada: sem amostra suficiente, o medidor mostra `Calculando autonomia…` ou `Calculando tempo…`; carga pausada e carga completa têm estados próprios.
- Chip abaixo do medidor foi simplificado para um estado curto (`Na bateria`, `Carregando`, `Conectado • carga pausada` ou `Carga completa`), reduzindo duplicação visual.
- Contraste do aro e do halo do medidor foi suavizado e o conteúdo interno reorganizado para abrir espaço à previsão sem aumentar o componente.
- Acessibilidade do medidor e do status foi atualizada para anunciar percentual e previsão quando aplicável.
- Placeholder visual antigo da versão em Configurações foi removido; o número exibido continua vindo em tempo de execução do pacote instalado.
- Versão, identidade, documentação, novidades e workflow sincronizados para 1.0.26+27.

## 1.0.25+26

- Iniciado o refinamento visual da tela Agora com superfícies e contornos mais discretos, reduzindo a competição visual entre os cards.
- Card de fonte reorganizado: `Fonte atual` passa a destacar o que o Android detectou e `Perfil configurado` fica separado como referência definida pelo usuário.
- Cards de Tensão, Corrente, Velocidade e Temperatura ganharam hierarquia mais limpa, etiquetas de origem discretas e estado visual atenuado quando a leitura não está disponível.
- Resumo da sessão passou a mudar conforme o estado: conectado mostra Tempo/Energia/Carga; fora da tomada mostra Tempo na bateria/Queda/Média de descarga usando somente dados reais da sessão.
- Navegação inferior recebeu contorno mais suave, seleção ativa mais compacta e tipografia padronizada em todas as abas.
- Status de carregamento deixou de usar uma cápsula verde excessivamente brilhante e passou a usar fundo verde escuro com texto de destaque, mantendo legibilidade no tema escuro.
- Valores ausentes na tela Agora passam a usar `—` e o card é atenuado, evitando poluição com a palavra `Indisponível`.
- Versão, identidade, documentação, novidades e workflow sincronizados para 1.0.25+26.

## 1.0.24+25

- Corrigida a leitura de corrente para preservar exatamente o sinal retornado por `BATTERY_PROPERTY_CURRENT_NOW`; removida a inversão artificial por estado de carregamento.
- Potência de carga só é calculada quando a corrente bruta é positiva e o Android informa estado de carregamento, evitando mascarar leituras inconsistentes.
- Tela Agora passa a mostrar autonomia estimada fora da tomada apenas com amostra mínima de 3 minutos e 1% de queda real; antes disso exibe `Calculando autonomia…`.
- Card `Velocidade de carga` muda automaticamente para `Velocidade de descarga` ao desconectar e mostra a taxa observada em `%/h`; o card de corrente passa a exibir a leitura instantânea com sinal e cor correspondente.
- A mesma regra de confiabilidade foi centralizada para Agora, Descarga, Consumo de bateria e notificação, evitando projeções precoces divergentes entre telas.
- Ranking de apps deixa de forçar participação mínima de 1% e passa a descrever a porcentagem como `atividade observada`, não como consumo elétrico.
- Removidos valores fictícios usados como placeholders em linhas de histórico, comparação e uso de apps; campos dinâmicos iniciam com `—`.
- Versão, identidade, documentação, novidades e workflow sincronizados para 1.0.24+25.

## 1.0.23+24

- Notificação persistente redesenhada como resumo vivo da bateria: na descarga mostra autonomia estimada e ritmo quando a amostra já é confiável; na carga mostra previsão aproximada até 100%.
- Removidos campos `Indisponível` da linha principal da notificação; leituras ausentes passam a ser simplesmente omitidas.
- Notificação expandida passa a exibir informações da sessão, como duração, variação percentual, média observada e corrente instantânea quando disponível.
- Adicionados estados específicos para carga completa, carga pausada, temperatura elevada e consumo elevado, sem inventar autonomia enquanto ainda não há amostra suficiente.
- Tela Consumo de bateria agora representa a direção da corrente: valor negativo em vermelho durante descarga e positivo em verde durante carregamento.
- Com o carregador conectado, Corrente instantânea deixa de mostrar `Pausado` e volta a exibir a leitura real disponível; a taxa de descarga fica marcada como não aplicável.
- Versão, identidade, documentação, novidades e workflow sincronizados para 1.0.23+24.

## 1.0.22+23

- Adicionado capturador global de exceções fatais para registrar automaticamente falhas que encerram o aplicativo, inclusive em threads de segundo plano.
- Cada falha salva data/hora, versão, tela ativa, thread, tipo/mensagem da exceção, stack trace, modelo do aparelho, Android, memória, armazenamento livre e estado recente da bateria.
- Relatórios são armazenados somente no aparelho, limitados aos 10 mais recentes, sem envio automático para servidor ou serviço externo.
- Tela Diagnóstico passou a incluir a última falha capturada, contador de relatórios, botão para exportar o diagnóstico completo em `.txt` e opção para limpar as falhas salvas.
- Após um fechamento inesperado capturado, a próxima abertura informa uma única vez onde o relatório pode ser encontrado.
- `Application` dedicada instalada no Manifest para iniciar o capturador antes das telas do app.
- Consultas de atividade por aplicativo agora tratam falhas inesperadas sem derrubar o processo, exibindo uma mensagem de tentativa novamente.
- Versão, identidade, documentação, novidades e workflow sincronizados para 1.0.22+23.

## 1.0.21+22

- Status verde de carregamento redesenhado: o texto genérico `Carregando` foi substituído por uma previsão direta como `100% em aproximadamente 3 h 12 min`; carga completa, carga pausada e uso na bateria também ganharam textos próprios.
- Removida a linha redundante de tempo restante acima do status, concentrando a informação principal em um único componente mais claro.
- Adicionado card `Consumo de bateria` no espaço inferior da tela Agora, com acesso rápido à nova análise.
- Criada tela Consumo de bateria com taxa de descarga atual, corrente instantânea observada, bateria atual e ranking dos apps mais ativos nas últimas 6 horas.
- Adicionado fluxo para conceder `Acesso ao uso` nas Configurações do Android; o ranking usa tempo em primeiro plano e não é apresentado como consumo elétrico exato por aplicativo.
- Adicionado acesso secundário ao módulo de consumo em Configurações e atualizadas as novidades exibidas uma vez após a atualização.
- Versão, identidade, documentação e workflow sincronizados para 1.0.21+22.

## 1.0.20+21

- Descarga promovida para a navegação principal, ao lado de Agora, Gráficos, Sessão e Histórico.
- Tela de Descarga aprimorada com projeção da bateria em 1 hora, média das descargas salvas e indicador de qualidade da amostra.
- Cabeçalho e mensagens da medição revisados para explicar melhor quando a taxa ainda está sendo calculada.
- Mantido o atalho de Descarga em Configurações como acesso secundário.
- Adicionado pop-up elegante de novidades, exibido automaticamente uma única vez por versão após a atualização.
- Versão, identidade, documentação e workflow sincronizados para 1.0.20+21.

## 1.0.19+20

- Adicionada tela **Taxa de descarga** para acompanhar o consumo da bateria quando o aparelho está fora do carregador.
- A nova medição registra bateria inicial/atual, percentual consumido, tempo observado, taxa média em `%/h` e autonomia estimada até 0%.
- Sessões de descarga são iniciadas automaticamente durante o monitoramento e concluídas quando uma fonte de energia volta a ser conectada.
- Adicionado histórico próprio de descargas concluídas, separado do histórico de carregamento, com data, duração, faixa de bateria e taxa média.
- A tela permite reiniciar a medição atual e limpar apenas o histórico de descarga; intervalos longos sem observação iniciam uma nova medição para evitar extrapolações falsas.
- Acesso à Taxa de descarga foi incluído em Configurações > Monitoramento, mantendo a arquitetura modular e o limite de 500 linhas por arquivo.

## 1.0.18+19

- Adicionada estimativa aproximada de tempo até 100% acima do status de carregamento; Android 9+ prioriza a previsão do sistema e há fallback conservador pelo ritmo da sessão quando houver dados suficientes.
- Tela Agora ganhou linguagem mais simples: `Potência calculada` passa a ser apresentada como `Velocidade de carga` e há ajuda explicando W, mA, Wh, mAh, tensão, temperatura e tempo.
- Aba Sessão foi reorganizada para abrir com um resumo fácil de bateria, tempo, energia, velocidade média e temperatura máxima; métricas avançadas ficam recolhidas em `Ver detalhes técnicos`.
- Ao atingir 100%, a tela Agora mostra `Carga completa` em vez de continuar exibindo apenas `Carregando`.
- Mantidos os cálculos originais, histórico, gráficos, diagnóstico, assinatura permanente, publicação somente do APK e limite de 500 linhas.

## 1.0.17+18

- Adicionada tela Diagnóstico em Configurações com versão, aparelho/Android, estado do monitoramento, permissão de notificações, fonte configurada, leitura atual, sessão, Histórico e amostras dos Gráficos.
- Diagnóstico pode ser atualizado e copiado para facilitar suporte sem incluir keystore, Secrets ou chaves de assinatura.
- Adicionado `tools/validate_project.py` e integrado ao Works antes do build para validar versão, XML, recursos/IDs, launcher, limpeza do source e limite de 500 linhas.
- Corrigido o encerramento manual do monitoramento contínuo para limpar a sessão persistida e impedir retomada indevida de uma sessão antiga ao iniciar o serviço novamente.
- Revisados os fluxos conectar/desconectar, carga completa, histórico automático, comparação, gráficos e persistência da sessão.

## 1.0.16+17

- Histórico agora permite selecionar exatamente duas sessões e abrir uma comparação lado a lado.
- Comparação inclui duração, tempo carregando, Wh, mAh, potência, corrente, tensão, temperatura, interrupções, ganho de bateria, estabilidade e relação com a potência nominal configurada.
- A tela mostra sempre a diferença B − A e evita declarar automaticamente uma sessão ou fonte como melhor.

## 1.0.15+16

- Ativada a aba Histórico e integrado o salvamento automático ao ciclo real da sessão: conectou inicia, desconectou salva.
- Cada registro preserva data/hora, perfil configurado, fonte detectada, duração, tempo carregando, Wh, mAh, potência média/pico, corrente/tensão, temperatura, interrupções e ganho de bateria.
- Histórico funciona tanto com o monitor local quanto com o Foreground Service, evitando exigir botão manual de salvar.
- Lista é ordenada da sessão mais recente para a mais antiga e limitada às 100 sessões mais recentes para controlar o armazenamento.
- A aba Histórico mantém o monitor local ativo quando necessário, permitindo registrar a desconexão mesmo enquanto o usuário consulta a própria lista.
- Preparada a estrutura persistente para a próxima etapa de comparação entre sessões.
- Mantidos assinatura permanente, publicação somente do APK no Works e limite de 500 linhas por arquivo.

## 1.0.14+15

- Corrigido o ciclo da sessão: uma nova sessão começa ao conectar a fonte e a tela Agora volta a `00:00:00`, sem carregar o tempo da sessão anterior.
- Ao desconectar fisicamente a fonte, a sessão atual é encerrada e o motor disponibiliza o snapshot concluído para o Histórico salvar automaticamente na próxima etapa.
- Pausas com a fonte ainda conectada permanecem na mesma sessão e são contabilizadas como interrupções.
- Ao atingir 100%, duração, Wh, mAh e estatísticas ficam congelados como carga completa até a desconexão, preservando o resultado final.
- Adicionado resumo inteligente na aba Sessão com potência atual/média/pico, comparação com a referência nominal e indicador de estabilidade baseado nas oscilações observadas.
- A leitura de conexão física foi separada do status de carregamento para diferenciar cabo conectado de carregamento temporariamente pausado.
- Mantidos assinatura permanente, publicação somente do APK no Works e limite de 500 linhas por arquivo.

## 1.0.13+14

- Ativada a aba Gráficos com visualizações leves de potência, corrente, temperatura e porcentagem da bateria.
- Adicionados períodos de 5, 15 e 60 minutos, mantendo a tela compacta em grade 2×2 e sem dependência externa de gráficos.
- Separada a frequência de leitura da frequência de armazenamento: o monitor continua lendo normalmente, enquanto os gráficos guardam no máximo uma amostra a cada 10 segundos.
- Amostras são limitadas aos últimos 60 minutos e persistidas em lote aproximadamente uma vez por minuto, reduzindo escritas, memória e consumo.
- Navegação Agora ↔ Gráficos ↔ Sessão agora está funcional; Histórico permanece reservado para a próxima etapa correspondente.
- Mantidos assinatura permanente, publicação somente do APK no Works e limite de 500 linhas por arquivo.

## 1.0.12+13

- Adicionado preenchimento rápido do Perfil da fonte com seletores de marca, modelo/preset, potência, protocolo, porta e saídas comuns, reduzindo a necessidade de digitação.
- Incluídas sugestões como Samsung EP-TA800 25 W, Samsung EP-T4510 45 W, Apple A2305 20 W, presets de painéis por potência e power banks por capacidade; opções manuais continuam disponíveis.
- Protocolos comuns agora podem ser selecionados, incluindo USB-PD, USB-PD 3.0, USB-PD 3.1, PPS, Quick Charge e outros.
- Tela Agora passou a comparar a potência calculada com a potência nominal configurada, identificando o resultado como observado no aparelho.
- Aba Sessão passou a contextualizar o pico da sessão em relação à potência nominal sem declarar eficiência ou potência direta da fonte.
- Mantidos publicação somente do APK no Works, assinatura permanente e limite de 500 linhas por arquivo.

## 1.0.11+12

- Ativada a aba Sessão na navegação inferior, mantendo Agora como tela principal sem rolagem.
- Criada tela detalhada da sessão atual com duração, tempo carregando, interrupções, Wh, mAh, potência média/pico, corrente e tensão médias/mínimas/máximas, temperatura e ganho de bateria.
- Navegação Agora ↔ Sessão passou a funcionar com destaque visual da aba ativa; Gráficos e Histórico continuam reservados para próximas etapas.
- A tela Sessão reutiliza o snapshot do motor de sessão já existente e deixa explícito que Wh/mAh e médias são estimados a partir das leituras do aparelho.
- Mantidos perfil técnico da fonte, assinatura permanente, publicação somente do APK no Works e limite de 500 linhas por arquivo.

## 1.0.10+11

- Criado motor de sessão com integração temporal das amostras para estimar Wh e mAh, sem multiplicar o último valor pela duração total.
- Adicionadas potência/corrente/tensão médias, mínimos e máximos, temperatura média/máxima, tempo efetivamente carregando, interrupções, bateria inicial/atual e ganho percentual.
- Intervalos longos, leituras ausentes, períodos sem carga e mudança da fonte detectada não são integrados, reduzindo valores falsos.
- Tela Agora passou a mostrar Tempo, Energia (Wh) e Carga (mAh) no resumo compacto, mantendo a tela sem rolagem.
- Estado da sessão do monitoramento contínuo é persistido para recuperação após reinício do serviço; após reinicialização completa do aparelho uma nova sessão é iniciada.
- Mantidos perfil técnico da fonte, assinatura permanente, publicação somente do APK no Works e limite de 500 linhas por arquivo.

## 1.0.9+10

- Aprimorado o Perfil da fonte para orientar o preenchimento com dados reais da etiqueta, sem exigir nome manual do carregador.
- Nome do perfil agora pode ser gerado automaticamente a partir de marca, modelo e potência, mantendo nome personalizado apenas como opção.
- Carregador passou a aceitar marca, modelo, potência máxima, saídas, protocolo/tecnologia, porta e dados opcionais do cabo.
- Power bank passou a aceitar capacidade, potência, saídas, protocolo e porta; painel solar ganhou tensão, corrente e controlador/conversor opcionais.
- Perfis antigos continuam compatíveis e são migrados ao serem lidos, sem apagar a configuração existente.
- Dados da etiqueta são tratados como referência nominal e não como medição direta da saída da fonte.
- Mantidos assinatura permanente, publicação somente do APK no Works e limite de 500 linhas por arquivo.

## 1.0.8+9

- Corrigido o nome do aplicativo no launcher: `Minha Bateria` agora é declarado por `@string/app_name` tanto no `application` quanto na Activity MAIN/LAUNCHER.
- Criada classificação central de origem das medições em Sistema, Calculado e Estimado.
- Tensão, corrente e temperatura são identificadas como dados do sistema; potência é identificada como valor calculado.
- Porcentagem e status indisponíveis deixaram de ser convertidos silenciosamente em `0%` ou `não carregando`; o app preserva estado indisponível.
- Adicionada em Configurações a seção Dados e medições, explicando a origem dos valores e que leituras ausentes permanecem como `Indisponível`.
- Mantidos monitoramento contínuo, perfil da fonte, assinatura release permanente e publicação somente do APK no Works/GitHub.

## 1.0.7+8

- Adicionado Perfil da fonte de energia com Painel solar, Carregador, Power bank e Outra fonte.
- Adicionados nome personalizado e potência nominal persistidos; para painel solar, a potência nominal é obrigatória.
- A primeira configuração é oferecida uma vez e pode ser ignorada; o perfil continua editável em Configurações.
- A tela Agora agora separa claramente o perfil informado pelo usuário da conexão detectada pelo Android.
- O workflow foi corrigido para publicar somente `Minha-Bateria-1.0.7.apk` como saída de entrega, sem criar ou publicar source ZIP no Works.
- Mantidos assinatura release permanente, layout principal sem rolagem e limite de 500 linhas por arquivo.

## 1.0.6+7

- Redesenhada a tela Agora com identidade visual azul-profundo inspirada na referência aprovada, sem alterar a lógica de monitoramento.
- Medidor circular ganhou aro externo, brilho sutil e melhor contraste; status de carregamento passou a usar ícone e cápsula com gradiente.
- Cards de fonte, tensão, corrente, potência, temperatura e resumo receberam nova hierarquia, ícones e bordas discretas.
- Navegação inferior foi refeita com ícones e destaque visual para a aba Agora, mantendo Gráficos, Sessão e Histórico reservados.
- Mantida a tela Agora sem rolagem, com dimensões responsivas para alturas menores e todos os arquivos abaixo de 500 linhas.
- Mantida a assinatura release permanente e a publicação direta do APK em GitHub Releases.

## 1.0.5+6

- Removido `actions/upload-artifact` para o APK, evitando que o download seja empacotado automaticamente como ZIP.
- APK release passou a ser publicado diretamente em GitHub Releases.

## 1.0.4+5

- Corrigidos os insets da barra de status e da barra de navegação do Android.
- Reorganizada a tela Agora para reduzir espaços vazios e manter o conteúdo agrupado sem rolagem.
- Ajustado contraste de valores indisponíveis e das abas futuras.

## 1.0.3+4

- Adicionada tela Configurações e seção Sobre.
- Monitoramento contínuo movido para Configurações.
- Adicionada doação por Pix com cópia para a área de transferência.
- Adicionada assinatura release permanente e build assinado.
