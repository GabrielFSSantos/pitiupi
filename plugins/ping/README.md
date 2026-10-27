# Plugin Ping (exercício de aula)

Material didático para o aluno **implementar** um plugin de Ping/Pong com medição de RTT. Este diretório no repositório oficial contém apenas planejamento e referências — **não** inclua código Java ou JAR da solução no commit.

## Onde está o tutorial

Tutorial canônico (passo a passo): [docs/documentation/17_tutorial_ping_aula.md](../../docs/documentation/17_tutorial_ping_aula.md).

## Como o aluno trabalha

1. Copiar a estrutura de [`plugins/chat`](../chat) para um projeto local (ou criar `plugins/ping` com `src/` apenas no fork pessoal).
2. Implementar `PingPlugin`, mensagens e `PingPanel` (`JPanel` + `bindShellWorkspaceContent`).
3. Build: `ant jar` → `dist/pitiupi.ping.jar`.
4. Instalar em `PITIUPI_HOME/plugins/` e reiniciar o Pitiupi.
5. Abrir o plugin pela aba **Plugins** na galeria (aba não abre sozinha no boot).

Quando o JAR estiver pronto para o time, registre uma entrada em [`plugins.registry.json`](../../plugins.registry.json) na raiz do clone (id `ping`, `dir`: `plugins/ping`, etc.).

## Pré-requisitos

- Núcleo compilado: `../../pitiupi/dist/pitiupi.jar`
- Leitura: [09_sistema_de_plugins.md](../../docs/documentation/09_sistema_de_plugins.md)
