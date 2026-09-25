# Changelog

## 1.0.4+5

- Corrigidos os insets da barra de status e da barra de navegação para impedir sobreposição com relógio, rede, bateria e botões do Android.
- Reagrupada a tela Agora para eliminar os grandes espaços vazios ao redor do medidor sem reintroduzir rolagem.
- Medidor, status, fonte, métricas e resumo da sessão ficaram visualmente mais próximos e compactos.
- Reduzidas alturas e espaçamentos dos painéis para melhorar o aproveitamento vertical em telas menores.
- Adicionados tamanhos responsivos por altura de tela, mantendo a tela Agora sem rolagem também em aparelhos mais compactos.
- Valores `Indisponível` passaram a usar cor neutra em vez de verde ou laranja.
- A barra inferior ganhou melhor contraste e indicação visual própria para a aba Agora, mantendo as abas futuras sem funcionalidade fictícia.
- A mesma correção de insets foi aplicada à tela de Configurações.
- Mantidos foreground service, assinatura release permanente e demais funcionalidades da versão anterior.
- Mantida arquitetura modular e limite máximo de 500 linhas por arquivo de código.

## 1.0.3+4

- Modernizado o visual da tela principal com fundo sutil em gradiente e cards com maior separação visual.
- Reorganizadas as quatro métricas em blocos individuais sem adicionar rolagem à tela Agora.
- Adicionada engrenagem de Configurações no canto superior direito.
- Removidos os controles de monitoramento contínuo da tela principal.
- Criada tela de Configurações reutilizando o serviço e as preferências já existentes.
- Monitoramento contínuo, iniciar/parar e retomar após reiniciar foram movidos para Configurações.
- Adicionada seção Sobre o app com versão instalada e as três alterações mais recentes.
- Adicionada seção Doação com cópia da chave Pix para a área de transferência e confirmação por mensagem.
- Alterado o workflow de `assembleDebug` para `assembleRelease` assinado.
- Configurada assinatura permanente por GitHub Secrets para permitir atualização sobre versões futuras assinadas com a mesma chave.
- Adicionada documentação de assinatura em `SIGNING.md`.
- Mantida arquitetura modular e limite máximo de 500 linhas por arquivo de código.

## 1.0.2+3

- Removido o `ScrollView` da tela principal.
- Reorganizada a tela Agora para caber integralmente na área útil disponível.
- Medidor circular reduzido e tornado responsivo à altura disponível.
- Corrigido o desenho do medidor para permanecer circular mesmo em containers não quadrados.
- Tensão, corrente, potência e temperatura reorganizadas em grade compacta 2x2.
- Tempo conectado e picos reunidos em um único resumo horizontal.
- Controles do monitoramento contínuo compactados sem remover funcionalidades.
- Adicionada barra inferior fixa reservando Agora, Gráficos, Sessão e Histórico.
- Layout principal dividido em painéis XML menores para evitar crescimento de um único arquivo.
- Gráficos, Sessão e Histórico permanecem inativos nesta versão; nenhuma lógica fictícia foi adicionada.
- Removidos layouts de linhas antigas que ficaram sem uso após a reorganização.
- Mantido o foreground service e toda a lógica de monitoramento da versão anterior.
- Mantido o limite de 500 linhas por arquivo.

## 1.0.1+2

- Adicionado foreground service para monitoramento contínuo.
- Adicionadas permissões de foreground service, special use, notificações e boot.
- Adicionada notificação permanente com bateria, corrente, potência e temperatura.
- Adicionados controles para iniciar e parar o monitoramento contínuo.
- Adicionada persistência do estado solicitado do monitoramento.
- Adicionada opção `Retomar após reiniciar`.
- Adicionado receiver de `BOOT_COMPLETED` condicionado à preferência do usuário e ao estado ativo anterior.
- A interface passa a reutilizar os dados da sessão do serviço quando o monitoramento contínuo está ativo.
- Mantido monitoramento local enquanto a tela está aberta e o serviço contínuo está inativo.
- Removido `KEEP_SCREEN_ON`; o aplicativo não força mais a tela a permanecer ligada.
- Notificação limitada a atualizações a cada 5 segundos para reduzir trabalho desnecessário.
- Mantida arquitetura modular e limite de 500 linhas por arquivo Kotlin.

## 1.0.0+1

- Projeto inicial do Minha Bateria.
- Interface escura baseada no mockup aprovado.
- Leitura de porcentagem, estado e fonte de carregamento.
- Leitura de tensão, corrente e temperatura.
- Cálculo de potência em watts.
- Cronômetro da sessão de carregamento.
- Pico de corrente e potência da sessão.
- Arquitetura modular com limite de 500 linhas por arquivo de código.
- Workflow de build para APK.
