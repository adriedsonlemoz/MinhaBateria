# Changelog

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
