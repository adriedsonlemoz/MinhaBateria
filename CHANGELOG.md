# Changelog

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
