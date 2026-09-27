# AI_INSTRUCTIONS.md — Manual operacional para alterações por IA

## 1. Regra principal

**Leia este arquivo antes de modificar qualquer arquivo do projeto.**

Este documento é o ponto de entrada para qualquer IA, agente de código ou automação que receber este projeto. Ele não substitui a documentação técnica. Ele define **como a IA deve trabalhar e como deve entregar o resultado**.

Nunca presuma uma regra do projeto quando ela puder ser confirmada pela documentação ou pelo código existente.

## 2. Ordem obrigatória de leitura

Antes de alterar qualquer coisa:

1. Leia `AI_INSTRUCTIONS.md` inteiro.
2. Leia `README.md`.
3. Leia `VERSION` para descobrir a versão atual.
4. Leia `VALIDATION.md` para conhecer as verificações da versão atual.
5. Leia `CHANGELOG.md` para entender alterações recentes e evitar regressões.
6. Leia `WORKS.md` e `RELEASE.md` quando a alteração envolver entrega, build, APK ou distribuição.
7. Leia `SIGNING.md` quando envolver assinatura, release ou APK.
8. Leia `INTERPRETATION.md` quando a alteração envolver medições, sensores, cálculos ou interpretação de dados.
9. Leia o código-fonte relacionado diretamente à alteração antes de editá-lo.

Se uma documentação contradizer o código atual, **não escolha silenciosamente uma versão**. Identifique a divergência e corrija a documentação ou o código de forma consistente, registrando a decisão no changelog/validação quando aplicável.

## 3. Como executar uma solicitação

Quando o usuário disser, por exemplo, **"mude X"**:

1. Identifique exatamente o comportamento que X deve mudar.
2. Localize o código, recurso, configuração ou ativo responsável.
3. Verifique as regras documentadas para essa área.
4. Preserve APIs, comportamentos e arquivos não relacionados sempre que possível.
5. Faça a menor alteração coerente com o objetivo.
6. Revise o diff e procure efeitos colaterais.
7. Execute as validações disponíveis.
8. Atualize a documentação afetada.
9. Atualize a versão de acordo com as regras deste arquivo.
10. Faça a conferência final dos arquivos.
11. Gere o pacote de entrega somente depois das verificações.

## 4. Versionamento

A versão canônica está em `VERSION`, no formato:

```text
versionName+versionCode
```

Exemplo:

```text
1.0.40+41
```

Ao entregar uma alteração funcional ou estrutural do projeto:

- incremente `versionName` conforme o impacto da alteração;
- incremente sempre o `versionCode`;
- mantenha `VERSION`, `app/build.gradle.kts`, `app_identity.json`, README, workflow, tela de novidades e documentos de release sincronizados;
- nunca reutilize `versionCode`;
- procure referências à versão anterior antes de finalizar.

Para qualquer alteração que crie uma nova entrega, a IA deve explicar no changelog o que mudou.

## 5. Validação obrigatória

Execute:

```bash
python3 tools/validate_project.py
```

Se a validação falhar, não declare a entrega como concluída. Corrija o problema ou informe claramente a falha.

Se o ambiente não possuir Android SDK/Gradle, informe que o build Android local não foi executado. Não diga que o APK foi validado se ele não foi realmente compilado.

## 6. Contagem e integridade dos arquivos

**A contagem final é obrigatória em toda entrega.**

Não basta comparar apenas o número total. A IA deve:

1. contar todos os arquivos do projeto antes da alteração, quando a versão anterior estiver disponível;
2. contar todos os arquivos depois da alteração;
3. comparar a lista completa de caminhos relativos;
4. identificar arquivos adicionados;
5. identificar arquivos removidos;
6. identificar arquivos que mudaram;
7. registrar a contagem final no relatório de entrega.

Arquivos removidos ou adicionados nunca devem ser ignorados apenas porque a contagem total permaneceu igual.

Para esta versão, a contagem deve ser conferida contra o pacote de origem usado como base.

## 7. Entrega do ZIP

O ZIP de source deve conter o projeto necessário para continuar o desenvolvimento.

Antes de criar o ZIP:

- não incluir `build/`;
- não incluir `.gradle/`;
- não incluir APK/AAB;
- não incluir keystore/chaves privadas;
- não incluir secrets;
- não incluir caches;
- não incluir arquivos temporários da IA.

Depois de criar o ZIP:

- abra/teste a listagem do ZIP;
- conte novamente os arquivos **dentro do ZIP**;
- compare os caminhos do ZIP com a árvore do projeto;
- confirme que a versão no nome/conteúdo corresponde à versão entregue.

## 8. Áudio

O projeto possui 24 OGG padrão mapeados em `VoiceAlertEvent.kt`. Não remova, renomeie ou substitua esses arquivos sem verificar o mapeamento e executar a validação.

Alterações de loudness, volume ou reprodução devem ser avaliadas considerando pico, loudness e clipping. Não aumente simplesmente o ganho até o limite digital.

## 9. Segurança e limpeza

Nunca coloque no source ZIP:

- senhas;
- tokens;
- secrets;
- chaves privadas;
- keystores;
- APK/AAB de release;
- credenciais pessoais.

## 10. Relatório final obrigatório

A resposta da IA após a entrega deve informar, de forma objetiva:

- versão anterior;
- versão nova;
- alteração realizada;
- validações executadas e resultado;
- contagem de arquivos antes;
- contagem de arquivos depois;
- quantidade de arquivos adicionados;
- quantidade de arquivos removidos;
- quantidade de arquivos modificados, quando calculável;
- se o build APK foi executado ou não;
- nome do arquivo ZIP entregue.

Se alguma etapa não puder ser executada, declare a limitação em vez de inventar um resultado.

## 11. Regra de encerramento

A tarefa só pode ser considerada concluída quando:

```text
[ ] solicitação implementada
[ ] documentação lida
[ ] código revisado
[ ] validação executada
[ ] versão incrementada
[ ] referências de versão sincronizadas
[ ] documentação atualizada
[ ] arquivos adicionados/removidos identificados
[ ] contagem final realizada
[ ] ZIP criado
[ ] conteúdo do ZIP conferido
[ ] relatório final informado ao usuário
```

**Se o usuário pedir apenas uma modificação de código, estas regras continuam valendo para a entrega do projeto.**
