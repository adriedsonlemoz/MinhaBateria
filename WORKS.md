# Works - instruções de build

1. Usar JDK 17.
2. Instalar/usar Android SDK 35.
3. Executar `gradle :app:assembleDebug` na raiz.
4. O APK fica em `app/build/outputs/apk/debug/app-debug.apk`.
5. Renomear o APK final para `Minha-Bateria-1.0.0.apk`.
6. APK deve permanecer fora do ZIP de código-fonte.
7. Antes de cada nova entrega, incrementar `versionName` e `versionCode` e sincronizar VERSION, app_identity.json, README e CHANGELOG.
8. Não permitir arquivos Kotlin acima de 500 linhas.
