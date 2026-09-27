# Works — Minha Bateria

Versão atual: `1.0.34+35`.

## Build recomendado

O projeto usa build `release` assinado com os Secrets permanentes do Minha Bateria. Antes de compilar, o workflow executa a validação estática do projeto.

```bash
python3 tools/validate_project.py
gradle :app:assembleRelease
```

Saída original: `app/build/outputs/apk/release/app-release.apk`

Nome de entrega: `Minha-Bateria-1.0.34.apk`

## GitHub Manager

Use o mesmo `Minha-Bateria-GitHub-Secrets.txt` já criado. A assinatura não deve ser trocada.

## Regras

- nenhum arquivo de código acima de 500 linhas;
- incrementar e sincronizar a versão a cada entrega;
- APK fora do ZIP de código-fonte;
- não incluir keystore ou Secrets no repositório;
- validar XML, Manifest, IDs e ZIP antes da entrega;
- o conteúdo principal de Agora deve caber no máximo possível sem exigir rolagem; detalhes técnicos podem expandir sob demanda;
- nenhum valor de bateria deve ser fabricado para preencher campos ausentes.

## Entrega

O Works/GitHub Actions publica somente `Minha-Bateria-1.0.34.apk`. O source ZIP é apenas o pacote de desenvolvimento entregue separadamente no chat.

## Atualização 1.0.34+35

- Agora, Sessão e Gráficos foram simplificados para leitura imediata por pessoas sem conhecimento elétrico.
- Perfil da fonte agora pergunta primeiro o tipo e mantém dados avançados recolhidos.
- Interpretações humanas usam critérios reais e centralizados, sem diagnosticar hardware como certeza.
- Gráficos e estimativas receberam proteções contra exagero visual e falsa precisão.
