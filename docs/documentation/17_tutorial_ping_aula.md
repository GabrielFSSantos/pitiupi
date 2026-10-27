# Tutorial de aula — plugin Ping

Material para o aluno implementar um plugin **Ping/Pong** com medição de RTT (round-trip time). A solução em Java **não** entra no repositório oficial; apenas este tutorial e a pasta [`plugins/ping/`](../../plugins/ping/) (README + `.gitignore`).

Índice geral: [README.md](README.md).

## Objetivos

- Estender `Plugin` e registrar SPI.
- Mensagens `Serializable` que estendem `Message`.
- `ConnectionManager`: multicast UDP, unicast UDP e TCP.
- UI na aba central: `JPanel` + `bindShellWorkspaceContent` (sem `JFrame`); abrir pela galeria **Plugins** ou peer actions.
- Instalar JAR em `PITIUPI_HOME/plugins` e testar com um colega.

## Pré-requisitos

- Pitiupi com login (nome + porta).
- [09_sistema_de_plugins.md](09_sistema_de_plugins.md).
- JDK, Ant; núcleo em `pitiupi/dist/pitiupi.jar`.

## Estrutura sugerida (projeto do aluno)

```
plugins/ping/                 # ou projeto fora do repo oficial
├── build.xml
├── nbproject/
├── src/
│   ├── META-INF/services/pitiupi.models.Plugin
│   └── br/ufsj/redes/ping/
│       ├── PingPlugin.java
│       ├── PingMessage.java
│       ├── PongMessage.java
│       └── PingPanel.java
└── dist/pitiupi.ping.jar
```

Modelo Ant: copiar [`plugins/chat`](../chat) e ajustar classpath para `../../pitiupi/dist/pitiupi.jar`.

## Passo 1 — Projeto Ant

1. Copiar `plugins/chat`.
2. JAR de saída: `dist/pitiupi.ping.jar`.
3. Classpath de compilação: JAR do núcleo.

**Verificação:** `ant jar` sem erros.

## Passo 2 — `PingPlugin`

| Método | Exemplo |
| --- | --- |
| `getName()` | `"Ping"` |
| `getDisplayName()` | `"Ping na rede"` |
| `getIdentifier()` | `"pitiupi.ping"` (único na sala) |

Em `createPlugin(ConnectionManager cm, PluginWorkspace workspace)`:

```java
pingPanel = new PingPanel(cm);
bindShellWorkspaceContent(pingPanel);
installPeerActions(workspace.peerActions());
```

**Não** chame `openOrFocusTab` no boot — a aba abre quando o usuário clicar na galeria ou usar peer action.

Peer actions sugeridas: “Enviar ping” (peer único), “Ping para todos”.

**Verificação:** após instalar o JAR e reiniciar, só **Plugins** e **Configurações** no boot; botão na galeria abre a aba Ping.

## Passo 3 — Mensagens

`PingMessage` e `PongMessage` com `pingId` (UUID) e timestamps para RTT.

Registrar em `getMessages()`:

```java
return List.of(PingMessage.class, PongMessage.class);
```

Em `receiveMessage`: ao receber ping, responder pong; ao receber pong, calcular RTT e atualizar a UI com `SwingUtilities.invokeLater`.

## Passo 4 — `PingPanel`

Botões para multicast, unicast UDP e TCP; área de log ou label com último RTT em ms. Para um peer, use campo IP como no `ChatPanel` ou peer action na lista lateral.

## Passo 5 — Transportes

Use `pitiupi.application.messaging.MessageDispatcher` no núcleo:

```java
MessageDispatcher.send(connectionManager, pingMessage, SendTarget.broadcastAll(), TransportMode.MULTICAST_UDP);
MessageDispatcher.send(connectionManager, pingMessage, SendTarget.singlePeer(addr), TransportMode.UNICAST_UDP);
MessageDispatcher.send(connectionManager, pingMessage, SendTarget.singlePeer(addr), TransportMode.TCP);
```

| Destino | Transporte didático | Observação |
| --- | --- | --- |
| Todos | Multicast UDP | Padrão em sala |
| Todos | Unicast UDP | Loop em cada peer online (demo) |
| Um peer | Unicast UDP / TCP | IP da lista ou manual |

Não alterar o endereço multicast do núcleo (`224.0.0.3`).

## Passo 6 — SPI e instalação

`META-INF/services/pitiupi.models.Plugin`:

```
br.ufsj.redes.ping.PingPlugin
```

Copiar JAR para `~/pitiupi/plugins/` (ou instalar pela aba Configurações) e **reiniciar**.

## Critérios de sucesso

| # | Critério |
| --- | --- |
| C1 | JAR carrega sem exceção |
| C2 | Pelo menos um transporte funciona entre duas instâncias |
| C3 | RTT em ms após o pong |
| C4 | README do aluno com build e transporte usado |

## Anti-padrões

- Commitar solução no repo oficial.
- Busy-wait na EDT.
- Objetos não `Serializable`.
- Mesmo `getIdentifier()` que outro plugin.

## Referências

- [10_tutorial_plugin.md](10_tutorial_plugin.md)
- [05_udp_multicast.md](05_udp_multicast.md)
- [06_tcp_conexoes.md](06_tcp_conexoes.md)
- [04_inicializacao_e_gui.md](04_inicializacao_e_gui.md)
