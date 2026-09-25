# Changelog

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
