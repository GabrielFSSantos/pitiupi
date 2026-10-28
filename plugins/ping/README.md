# Plugin Ping (exercício local)

Pasta opcional para desenvolver um módulo de Ping/Pong com medição de RTT. O código de solução **não** faz parte do gate CI do repositório.

Tutorial passo a passo: [docs/tp/02_tutorial_ping_aula.md](../../docs/tp/02_tutorial_ping_aula.md).

## Fluxo de trabalho

1. Copiar a estrutura de [`plugins/chat`](../chat) para `plugins/ping` (local).
2. Implementar `PingPlugin`, mensagens e painel (`bindShellWorkspaceContent`).
3. `ant jar` → instalar JAR em `PITIUPI_HOME/plugins/` e reiniciar o Pitiupi.
4. Abrir o plugin pela galeria **Plugins**.

## Pré-requisitos

- Núcleo: `../../pitiupi/dist/pitiupi.jar`
- SPI e mensagens: [09_sistema_de_plugins.md](../../docs/documentation/09_sistema_de_plugins.md)
