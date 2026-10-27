# Tutorial de aula — plugin Ping

Exercício para montar o plugin **mais simples** do Pitiupi: medir **RTT** (tempo de ida e volta) com mensagens `Ping` / `Pong` na rede, igual a um `ping` de laboratório — mas usando o **mesmo mecanismo de plugins** do chat e do screencast.

A solução Java **não** entra no Git: só este tutorial e [`plugins/ping/README.md`](../../plugins/ping/README.md). O professor valida localmente; o aluno implementa no próprio `plugins/ping/` (gitignored).

Índice: [README.md](README.md) · Plugins de referência: [11_plugins_referencia.md](11_plugins_referencia.md).

## O que você vai aprender

| Componente do núcleo | Para que serve no Ping |
| --- | --- |
| `Plugin` + SPI | O Pitiupi descobre seu JAR e chama `createPlugin` |
| `Message` serializável | `PingMessage`, `PongMessage`, `PingStopMessage` |
| `getMessages()` / `receiveMessage` | O núcleo entrega só os tipos que você registrou |
| `MessageDispatcher` | Envia UDP unicast/multicast sem montar socket manual |
| `PluginWorkspace` | Galeria e peer actions abrem a aba central |
| `PeerAction` | Menu ao clicar em um usuário na lista lateral |
| `SessionTabBar` | Abas por peer (passivas ao receber, como no chat) |

## Limitações didáticas (de propósito)

- Não é ICMP do sistema operacional — são mensagens Java na LAN.
- Sem gráficos nem histórico em arquivo.
- Transporte padrão: **UDP unicast** para um peer; multicast só no “ping para todos (uma vez)”.
- Não há moderação, login extra nem criptografia.

## Pré-requisitos

1. Pitiupi abrindo com login (nome + porta).
2. [`09_sistema_de_plugins.md`](09_sistema_de_plugins.md).
3. JDK 17 + Ant.
4. Núcleo compilado: `pitiupi/dist/pitiupi.jar`.

## Passo 1 — Projeto Ant

1. Copie `plugins/chat` para `plugins/ping` (somente na sua máquina).
2. Renomeie o projeto para `pitiupi.ping` e o JAR de saída para `dist/pitiupi.ping.jar`.
3. Classpath de compilação: `../../pitiupi/dist/pitiupi.jar`.

**Verificação:** `ant jar` sem erros.

## Passo 2 — SPI

Arquivo `src/META-INF/services/pitiupi.models.Plugin`:

```
pitiupi.ping.plugin.PingPlugin
```

## Passo 3 — Mensagens

Três classes em `pitiupi.ping.net`, todas estendendo `pitiupi.models.Messages.Message`:

1. **`PingMessage`** — campos: `pingId` (UUID), `sentAtMillis`, `senderName`, `senderIp`, `mode` (`ONCE` ou `CONTINUOUS_TICK`), `sessionId` (para série contínua).
2. **`PongMessage`** — `pingId`, `echoSentAtMillis` (copiado do ping), `responderName`, `responderIp`.
3. **`PingStopMessage`** — `sessionId`, `senderName`, `senderIp` (avisa que o contínuo parou).

Registre em `PingPlugin.getMessages()`:

```java
return List.of(PingMessage.class, PongMessage.class, PingStopMessage.class);
```

**Por quê:** o núcleo só desserializa tipos declarados; evita que outro plugin receba seus bytes por engano.

## Passo 4 — `PingController` (lógica)

Classe em `pitiupi.ping.control` **sem Swing**:

- Ao **enviar** ping: grave `pendingByPingId.put(pingId, sentAtMillis)` e use `MessageDispatcher.send(..., UNICAST_UDP)`.
- Ao **receber** `PingMessage`: responda com `PongMessage` (eco do `sentAtMillis`).
- Ao **receber** `PongMessage`: `rtt = now - pending.remove(pingId)` e notifique a UI.
- **Contínuo:** `javax.swing.Timer` a cada 1 s envia `CONTINUOUS_TICK` com o mesmo `sessionId`.
- **Parar:** cancele o timer local e envie `PingStopMessage` ao peer.

Use `PeerIpv4` e `LocalNetworkInfo.bestLocalIpv4()` para IP legível no Windows.

## Passo 5 — `PingPanel` (UI)

Siga o mesmo chrome do screencast: `PluginSessionWorkspace` + `PluginRoutingPanel` (`PluginKind.PING`, sem TCP privado).

- **Destino e transporte** no topo (IP, forma de envio, banner da sessão).
- Toolbar: **Enviar ping** (uma vez) e **Ping contínuo** (toggle só em abas de peer **↑**; na sala **Todos** só o envio único).
- `SessionTabBar`: sala `Todos ↑`; peers que você abre `Nome ↑`; recebimento passivo `Nome ↓` (`view:<ip>`).
- `+ Nova` / `PeerDraftPanel` para IP manual.
- `JTextArea` monoespaçada por aba; linhas como `[HH:mm:ss] pong ← Alice · RTT 42 ms`.
- Abas **↓**: `bindReceiveSession` — controles de envio desabilitados.

**Não** use `JFrame`. No `PingPlugin`: `bindShellWorkspaceContent(panel)`.

A aba da shell só abre quando o usuário clicar na galeria **Plugins** ou numa peer action (sem enviar ping automaticamente).

## Passo 6 — `PingPlugin`

| Método | Valor |
| --- | --- |
| `getIdentifier()` | `pitiupi.ping` (único na sala) |
| `getDisplayName()` | `Ping na rede` |

`createPlugin(ConnectionManager, PluginWorkspace)`:

1. Crie `PingController` e `PingPanel`.
2. `bindShellWorkspaceContent(panel)`.
3. `installPeerActions(workspace.peerActions())`.

**Peer actions sugeridas:**

| id | Label | Alvo | Comportamento |
| --- | --- | --- | --- |
| `ping.peer` | Enviar ping para esta pessoa | um peer | Abre/foca o plugin e a aba `↑` com o IP — **sem** disparar ping |
| `ping.room` | Abrir ping na sala (Todos) | Todos | Abre a aba sala — **sem** disparar ping |

O envio fica nos botões do painel.

`receiveMessage`: despache para o controller com `SwingUtilities.invokeLater`.

`onShellTabClosed`: pare todos os timers contínuos.

## Passo 7 — Instalar e testar

1. `ant jar` → `dist/pitiupi.ping.jar`.
2. Aba **Configurações** → **Instalar JAR…** (ou copie para `~/pitiupi/plugins/`).
3. **Reinicie** o Pitiupi (obrigatório após mudar plugins).
4. Dois processos no mesmo PC: portas **5000** e **5001**, nomes diferentes.
5. Lista lateral → **Enviar ping para esta pessoa** → aba `↑` com IP; clique **Enviar ping** → RTT no log.
6. Toggle **Ping contínuo** na aba `↑` → linhas a cada ~1 s; desligar o toggle → o outro vê “parou o ping contínuo” na aba `↓`.
7. Com colega na LAN: mesma porta, JAR instalado nos dois.

Instalação usa o mesmo `PluginStore` da aba **Configurações** (`installFromFile` + reinício). O núcleo expõe `pitiupi.tools.PluginStoreCli` para testes sem interface gráfica.

## Passo 8 — Comparar com o chat

O chat usa a mesma ideia de abas de sessão, mas adiciona roteamento TCP privado e HTML. No Ping mantenha **só** UDP e texto em `JTextArea` — ideal para entender o fluxo antes de copiar padrões para um plugin novo.

## Checklist do aluno

- [ ] SPI e `getIdentifier` únicos
- [ ] Três mensagens registradas
- [ ] RTT calculado no `Pong`
- [ ] Aba passiva `↓` ao receber ping de peer novo (sem roubar foco da shell)
- [ ] Contínuo + parar com aviso ao peer
- [ ] Reinício após instalar JAR
- [ ] Teste com segundo usuário (5001 ou outro PC)
