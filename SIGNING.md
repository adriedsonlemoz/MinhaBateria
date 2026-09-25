# Assinatura permanente

A partir da versão 1.0.3+4, o APK de distribuição é um `release` assinado com a chave permanente do projeto.

O arquivo `Minha-Bateria-GitHub-Secrets.txt` é entregue separadamente do código-fonte e contém os quatro Secrets necessários:

- `MINHA_BATERIA_KEYSTORE_BASE64`
- `MINHA_BATERIA_KEYSTORE_PASSWORD`
- `MINHA_BATERIA_KEY_ALIAS`
- `MINHA_BATERIA_KEY_PASSWORD`

Importe esses valores como GitHub Secrets. O workflow recria o keystore temporariamente durante o build e gera o APK da versão atual (nesta entrega, `Minha-Bateria-1.0.5.apk`).

Nunca publique o arquivo de Secrets nem o adicione ao repositório. Guarde uma cópia segura: versões futuras precisam usar a mesma chave para atualizar o aplicativo instalado sem desinstalá-lo.


O APK assinado é publicado diretamente em GitHub Releases. A assinatura não muda com esta alteração de distribuição.
