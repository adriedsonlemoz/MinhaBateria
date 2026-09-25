# Works — Minha Bateria

Versão atual: `1.0.3+4`.

## Build recomendado

O projeto passou a usar build `release` assinado. Antes do build, disponibilize estas variáveis de ambiente:

- `MINHA_BATERIA_KEYSTORE_PATH`: caminho do keystore recriado a partir do Secret Base64;
- `MINHA_BATERIA_KEYSTORE_PASSWORD`;
- `MINHA_BATERIA_KEY_ALIAS`;
- `MINHA_BATERIA_KEY_PASSWORD`.

Com os Secrets configurados:

```bash
gradle :app:assembleRelease
```

Saída original:

`app/build/outputs/apk/release/app-release.apk`

Nome de entrega:

`Minha-Bateria-1.0.3.apk`

## GitHub Manager

Importe o arquivo separado `Minha-Bateria-GitHub-Secrets.txt`. Ele contém os quatro Secrets no formato `NOME=VALOR` utilizado pelo workflow.

A mesma chave de assinatura deve ser preservada em todas as próximas versões. Trocar a chave impedirá a atualização sobre o APK já instalado.

## Regras do projeto

- não ultrapassar 500 linhas por arquivo de código;
- incrementar e sincronizar a versão a cada entrega;
- manter APK fora do ZIP de código-fonte;
- não adicionar o keystore ou o arquivo de Secrets ao repositório;
- validar XML, Manifest, IDs e integridade do ZIP antes da entrega.
