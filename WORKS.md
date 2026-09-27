# Works — Minha Bateria

Versão atual: `1.0.32+33`.

## Build recomendado

O projeto usa build `release` assinado com os Secrets permanentes do Minha Bateria. Antes de compilar, o workflow executa a validação estática do projeto.

```bash
python3 tools/validate_project.py
gradle :app:assembleRelease
```

Saída original: `app/build/outputs/apk/release/app-release.apk`

Nome de entrega: `Minha-Bateria-1.0.32.apk`

## GitHub Manager

Use o mesmo `Minha-Bateria-GitHub-Secrets.txt` já criado. A assinatura não deve ser trocada.

## Regras

- nenhum arquivo de código acima de 500 linhas;
- incrementar e sincronizar a versão a cada entrega;
- APK fora do ZIP de código-fonte;
- não incluir keystore ou Secrets no repositório;
- validar XML, Manifest, IDs e ZIP antes da entrega;
- tela Agora sem rolagem;
- nenhum valor de bateria deve ser fabricado para preencher campos ausentes.

## Entrega

O Works/GitHub Actions publica somente `Minha-Bateria-1.0.32.apk`. O source ZIP é apenas o pacote de desenvolvimento entregue separadamente no chat.

## Atualização 1.0.32+33

- novo padrão visual aplicado a todos os gráficos da tela Gráficos;
- cabeçalho de valor atual, linha contextual, resumo rápido e eixo mais legível;
- painel técnico da tela Sessão redesenhado em quatro blocos temáticos;
- tela de novidades atualizada e sincronizada com a nova entrega.
