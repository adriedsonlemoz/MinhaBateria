# Works — Minha Bateria

Versão atual: `1.0.7+8`.

## Build recomendado

O projeto usa build `release` assinado. Antes do build, disponibilize:

- `MINHA_BATERIA_KEYSTORE_PATH`;
- `MINHA_BATERIA_KEYSTORE_PASSWORD`;
- `MINHA_BATERIA_KEY_ALIAS`;
- `MINHA_BATERIA_KEY_PASSWORD`.

Com os Secrets configurados:

```bash
gradle :app:assembleRelease
```

Saída original: `app/build/outputs/apk/release/app-release.apk`

Nome de entrega: `Minha-Bateria-1.0.7.apk`

## GitHub Manager

Use o mesmo arquivo separado `Minha-Bateria-GitHub-Secrets.txt` criado anteriormente. A mesma chave de assinatura deve ser preservada em todas as versões futuras.

## Regras do projeto

- nenhum arquivo de código acima de 500 linhas;
- incrementar e sincronizar a versão a cada entrega;
- manter APK fora do ZIP de código-fonte;
- não adicionar keystore ou Secrets ao repositório;
- validar XML, Manifest, IDs e integridade do ZIP antes da entrega;
- preservar a tela Agora sem rolagem.

## Entrega do Works / GitHub Actions

O workflow publica **somente**:

- `Minha-Bateria-1.0.7.apk` — instalação direta.

O workflow não cria, não publica e não oferece `Minha-Bateria-1.0.7-source.zip` como saída. O source ZIP é apenas o pacote de desenvolvimento entregue separadamente fora do Works.

Não usar `actions/upload-artifact` para o APK.
