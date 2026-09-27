# Interpretação das medições — Minha Bateria 1.0.34+35

A interface separa quatro origens de informação:

- **medido pelo Android:** porcentagem, tensão, corrente, temperatura, estado e tipo de conexão quando o sistema fornece esses dados;
- **configurado pelo usuário:** tipo/modelo da fonte, potência nominal, capacidade e dados da etiqueta;
- **calculado pelo app:** potência instantânea (`V × A`), médias, mínimos/máximos, Wh, mAh e variações da sessão;
- **estimado/interpretado:** tempo restante, ritmo `%/h` e classificações humanas de carga/temperatura.

Nenhuma interpretação abaixo diagnostica defeito físico com certeza. Mensagens sobre cabo, fonte, temperatura ou iluminação são apresentadas como causas possíveis a verificar.

## Situação da carga

A implementação central fica em `ChargeConditionInterpreter` e é reutilizada nas telas Agora e Sessão.

Prioridade dos critérios:

1. **Aparelho muito quente:** temperatura `>= 45 °C`.
2. **Aparelho quente:** temperatura `>= 42 °C`.
3. **Sem fonte / usando a bateria:** Android informa desconectado.
4. **Carga pausada:** conectado, mas Android não informa carregamento ativo.
5. **Possível perda de carga:** durante carregamento, corrente bruta `< -50 mA`.
6. **Carga muito instável:** variação relativa de potência da sessão `>= 45%`.
7. **Carga oscilando:** variação relativa de potência da sessão `>= 25%`.
8. **Carga lenta:** após pelo menos 2 minutos de sessão, potência média/observada entre `0` e `1,0 W`.
9. **Abaixo da referência configurada:** após pelo menos 2 minutos, potência observada abaixo de `20%` da potência nominal configurada, com diferença absoluta mínima de `3 W`.
10. **Carregando / coletando:** carga ativa, mas ainda sem amostras suficientes para avaliar estabilidade.
11. **Carga normal:** nenhuma condição de atenção acima foi encontrada e já existe medida de variação.

A comparação com potência nominal é apenas contexto. O Android mostra o que é observado no aparelho; isso não é um wattímetro na saída da fonte e não deve ser chamado de eficiência.

## Temperatura

- abaixo de `38 °C`: normal;
- `38–41,9 °C`: morna;
- `42–44,9 °C`: quente;
- `>= 45 °C`: muito quente.

A exibição evita criar precisão falsa: se a leitura recebida for efetivamente inteira, a interface mostra, por exemplo, `42 °C`, e não `42,0 °C`.

## Gráficos

`ChartInsightBuilder` interpreta somente os pontos existentes na janela selecionada. Sem pontos suficientes, a tela informa que ainda está coletando.

- **Bateria:** informa estabilidade, ganho ou perda pelo primeiro/último valor válidos.
- **Corrente:** preserva o sinal do Android; média negativa recebe explicação de que houve consumo líquido no período. Janelas com sinais positivo e negativo são tratadas como alternância de entrada/saída.
- **Potência:** usa a dispersão relativa entre mínimo, máximo e média para qualificar estabilidade/oscilação.
- **Temperatura:** usa a faixa realmente observada e os mesmos limiares térmicos da interface principal.

O gráfico de bateria usa escala adaptativa com no mínimo 12 pontos percentuais de amplitude. Corrente mantém zero como referência e marca explicitamente a linha zero quando existem valores dos dois lados.

## Estimativa até 100%

A previsão não é apresentada como certeza. O app prioriza uma estimativa válida fornecida pelo Android. Na ausência dela, só calcula pelo ritmo da sessão depois de pelo menos **2 pontos percentuais de ganho e 2 minutos de carga**. Antes disso mostra que a estimativa está em preparação.
