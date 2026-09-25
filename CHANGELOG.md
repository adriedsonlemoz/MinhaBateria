# Changelog

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
