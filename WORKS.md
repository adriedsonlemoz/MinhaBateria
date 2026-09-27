# Works — Minha Bateria

Versão atual: `1.0.40+41`.

## Build recomendado

O projeto usa build `release` assinado com os Secrets permanentes do Minha Bateria. Antes de compilar, o workflow executa a validação estática do projeto.

```bash
python3 tools/validate_project.py
gradle :app:assembleRelease
```

Saída original: `app/build/outputs/apk/release/app-release.apk`

Nome de entrega: `Minha-Bateria-1.0.40.apk`

## GitHub Manager

Use o mesmo `Minha-Bateria-GitHub-Secrets.txt` já criado. A assinatura não deve ser trocada.

## Regras

- nenhum arquivo de código acima de 500 linhas;
- incrementar e sincronizar a versão a cada entrega;
- APK fora do ZIP de código-fonte;
- não incluir keystore ou Secrets no repositório;
- validar XML, Manifest, IDs e ZIP antes da entrega;
- nenhum valor de bateria deve ser fabricado para preencher campos ausentes;
- conexão AC/USB detectada pelo Android não pode ser apresentada como identificação física da fonte;
- exclusão de sessão exige confirmação e não pode apagar outros perfis/sessões;
- comparações devem distinguir total acumulado, potência e duração sem declarar automaticamente maior eficiência.

## Entrega

O Works/GitHub Actions publica somente `Minha-Bateria-1.0.40.apk`. O source ZIP é o pacote de desenvolvimento entregue separadamente.

## Atualização 1.0.40+41

- Avisos OGG remasterizados com compressão dinâmica e limiter, elevando o loudness percebido dos 24 eventos sem depender apenas de aumentar o pico digital. A medição do conjunto passou de cerca de -16,6 LUFS para -12,4 LUFS em média.
- Controle de volume padrão dos avisos passou de 85% para 100%; valores personalizados já salvos continuam preservados.
- Reprodução dos avisos passou a usar `USAGE_MEDIA` com `CONTENT_TYPE_SPEECH`, alinhando o fluxo de voz ao comportamento de áudio de mídia e mantendo a fila existente sem sobreposição.
- Tela de novidades, Release, Validation, README, identidade, Gradle, VERSION e workflow sincronizados para 1.0.40+41.

## Atualização 1.0.38+39

- Volume dos 24 arquivos OGG de avisos de voz normalizado individualmente (pico ajustado para ~-1 dB), aumentando o volume percebido sem distorcer o áudio.
- Tela de novidades atualizada para citar o ajuste de volume.

## Atualização 1.0.37+38

- 24 avisos de voz padrão integrados e validados.
- Áudio e avisos com volume, segundo plano, controles por evento, teste e arquivos personalizados.
- Fallback automático para OGG padrão quando o áudio personalizado não puder ser usado.
- Fila de voz sem sobreposição, prioridade, cooldown e transições de estado.
- Histórico com Comparar e Gerenciar independentes, seleção múltipla e exclusão total.
- Diálogos de exclusão e fonte conectada personalizados.

## Atualização 1.0.36+37

- Perfil da fonte compactado e com seletores inteligentes.
- Vários perfis podem ser preservados e escolhidos rapidamente ao conectar alimentação.
- X-TRAD SH-106 foi adicionado como preset editável de painel solar.
- Eventos de conexão/desconexão passaram a alimentar a seleção da fonte física sem confundir detecção do Android com o equipamento real.
- Reconexões rápidas não fragmentam a sessão.
- Histórico e Comparar cargas possuem exclusão segura com confirmação.
- Tipografia revisada nas seis telas principais do fluxo de carga.
