# Assinatura permanente

Desde a versão 1.0.3+4, o APK de distribuição é um `release` assinado com a chave permanente do projeto.

O arquivo `Minha-Bateria-GitHub-Secrets.txt`, entregue separadamente, contém os quatro Secrets usados pelo workflow. O keystore é reconstruído temporariamente no build e não deve entrar no repositório ou no source ZIP.

Nesta entrega o workflow gera `Minha-Bateria-1.0.12.apk`.

Versões futuras precisam usar a mesma chave para atualizar o aplicativo instalado sem desinstalá-lo.
