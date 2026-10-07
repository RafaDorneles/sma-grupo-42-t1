# Simulador de Redes de Filas (T1 – Simulação e Métodos Analíticos)

Simulador de eventos discretos para redes de filas com topologia genérica, em Java puro (sem bibliotecas externas).

## Requisitos
- Java 11 ou superior (JDK com `javac`).

## Compilar e executar
```
javac -d out src/*.java
java -cp out Main modelo.yml
java -cp out Main teste_fila_simples.yml
```
No PowerShell, se `src/*.java` não expandir: `javac -d out (Get-ChildItem src\*.java).FullName`.

## Formato do YAML
```yaml
arrivals:            # filas com chegada externa e o tempo da 1ª chegada
  Q1: 2.0
queues:
  Q1:
    servers: 1
    minArrival: 2.0  # obrigatório só para filas listadas em arrivals
    maxArrival: 4.0
    minService: 1.0
    maxService: 2.0
  Q2:
    servers: 2
    capacity: 5      # sem capacity = capacidade infinita
    minService: 4.0
    maxService: 6.0
network:
  - source: Q1
    target: Q2       # ou OUT para saída explícita
    probability: 0.2
rndnumbersPerSeed: 100000
seed: 12345          # opcional (padrão 12345)
# rndnumbers: [0.3, 0.5]   # opcional: lista fixa no lugar do gerador (testes)
```
- Se a soma das probabilidades de saída de uma fila for < 1, o restante sai do sistema; se for > 1, é erro.
- Subconjunto de YAML suportado: mapas, blocos indentados, listas `- item` / `- chave: valor` (inclusive com `-` na mesma indentação da chave pai), listas inline `[a, b]` e comentários `#`.

## Gerador de números aleatórios
Congruente linear: `X(i+1) = (1664525·X(i) + 1013904223) mod 2^32`, retornando `X/2^32` em [0,1). Uniforme(a,b) = a + (b−a)·rnd. Ao exceder o limite lança `FimDosAleatoriosException`.

## Regras da simulação
- Filas começam vazias; a 1ª chegada ocorre no tempo de `arrivals`, sem consumir aleatório.
- O laço roda enquanto houver eventos e o gerador não estiver esgotado; se a exceção ocorrer no meio de um evento, a simulação encerra.
- Escalonador ordena por tempo e, em empate, pela ordem de agendamento.
- A cada evento, `(tempoEvento − tempoAtual)` é somado ao tempo do estado atual de **todas** as filas, e o relógio avança.
- **Chegada** em F: (1) cliente entra em F; (2) agenda próxima chegada em `t + U(minArrival, maxArrival)`.
- **Cliente entra** em F: se há espaço, `clientes++` e, se `clientes <= servidores`, agenda SAÍDA em `t + U(minService, maxService)`; senão `perdas++`.
- **Saída** de F: (1) `clientes--`; (2) se `clientes >= servidores`, agenda nova SAÍDA; (3) escolhe destino: com uma única rota não consome aleatório (a rota é seguida diretamente); com várias, sorteia `r` e escolhe a primeira rota com `r < acumulado`; (4) se o destino for fila, o cliente entra imediatamente.
- Capacidade infinita: o vetor de tempos por estado cresce dinamicamente.

## Classes
| Classe | Papel |
|---|---|
| `Main` | Lê o argumento, carrega o modelo, simula e imprime |
| `Modelo` | Monta filas, rotas, primeiras chegadas e gerador a partir do YAML, validando campos e nomes |
| `LeitorYaml` | Parser do subconjunto de YAML |
| `Fila` | Configuração (servidores, capacidade, intervalos, rotas) e estado (clientes, perdas, tempos por estado) |
| `Rota` | Destino (`Fila` ou `null` = sai) e probabilidade |
| `Evento` / `TipoEvento` | Evento (tempo, tipo, fila, sequência) e enum `CHEGADA`/`SAIDA` |
| `Escalonador` | `PriorityQueue` por tempo e sequência: `agendar`, `proximo`, `vazio` |
| `Simulador` | Laço principal e tratamento dos eventos |
| `GeradorAleatorio` / `FimDosAleatoriosException` | Gerador congruente linear com contagem e limite |
| `Relatorio` | Imprime tabelas por fila, perdas, tempo global e aleatórios usados |
