# Assinatura permanente

Desde a versão 1.0.3+4, o APK de distribuição é um `release` assinado com a chave permanente do projeto.

O arquivo `Minha-Bateria-GitHub-Secrets.txt`, entregue separadamente, contém:

- `MINHA_BATERIA_KEYSTORE_BASE64`
- `MINHA_BATERIA_KEYSTORE_PASSWORD`
- `MINHA_BATERIA_KEY_ALIAS`
- `MINHA_BATERIA_KEY_PASSWORD`

O workflow recria o keystore temporariamente durante o build e gera, nesta entrega, `Minha-Bateria-1.0.7.apk`.

Nunca publique o arquivo de Secrets ou o keystore. Versões futuras precisam usar a mesma chave para atualizar o aplicativo instalado sem desinstalá-lo.
