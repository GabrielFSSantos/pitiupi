# Tutorial — construir o plugin Ping

Este texto monta o plugin que mede o tempo de ida e volta (RTT) com mensagens Java na rede local. Não é o `ping` do sistema operacional: são objetos `PingMessage` e `PongMessage` serializados, no mesmo mecanismo do chat.

O resultado pronto deste repositório está em `plugins/ping`. Os passos dizem qual arquivo criar, o que escrever e qual função do núcleo entra em cada momento. Se `plugins/ping` já existe, não copie outra pasta por cima: abra o arquivo citado e confira o trecho. Para refazer do zero numa pasta vazia, comece pelo passo do projeto Ant.

Leia antes, em uma sentada curta:

- [comecar.md](../comecar.md) — o programa abre e o núcleo já está compilado (`pitiupi/dist/pitiupi.jar`).
- [plugins.md](../plugins.md) — por que existe o arquivo de texto do SPI.
- [nucleo-para-plugins.md](../nucleo-para-plugins.md) — `Plugin`, `MessageDispatcher`, peer actions.

## O que vai existir no fim

| Arquivo | Papel |
| --- | --- |
| `src/META-INF/services/pitiupi.models.Plugin` | Diz ao Java o nome da classe do plugin |
| `src/pitiupi/ping/net/PingMode.java` | `ONCE` ou `CONTINUOUS_TICK` |
| `src/pitiupi/ping/net/PingMessage.java` | Ida: id, instante, nome, IP, modo, sessão |
| `src/pitiupi/ping/net/PongMessage.java` | Volta: o mesmo id e o instante original |
| `src/pitiupi/ping/net/PingStopMessage.java` | Avisa que o ping contínuo parou |
| `src/pitiupi/ping/control/PingLogSink.java` | A tela recebe linhas de log |
| `src/pitiupi/ping/control/PingController.java` | Envia, responde e calcula o RTT, sem desenhar botão |
| `src/pitiupi/ping/GUI/PingPanel.java` | Abas, botões e a área de texto |
| `src/pitiupi/ping/plugin/PingPlugin.java` | Liga tudo ao núcleo |

Limites de propósito: sem gráfico, sem arquivo de histórico, sem TCP. Um peer usa UDP unicast. A sala (**Todos**) pode usar multicast ou uma cópia unicast por pessoa. O ping contínuo (um por segundo) só existe na aba de uma pessoa.

## Passo 1 — Projeto Ant

O chat já é um projeto NetBeans/Ant com classpath no núcleo. Copiar essa casca evita escrever `build.xml` à mão.

Numa pasta `plugins/ping` ainda vazia:

```bash
cp -a plugins/chat plugins/ping
rm -rf plugins/ping/src/pitiupi plugins/ping/build plugins/ping/dist
```

O `rm` tira as classes do chat. O `build.xml`, o `nbproject/` e a pasta `src/` ficam.

Troque o nome do projeto em três arquivos. Em cada um, `chat` vira `ping`.

`plugins/ping/nbproject/project.xml`, a tag do nome (por volta da linha 6):

```xml
<name>pitiupi.ping</name>
```

`plugins/ping/build.xml`, a tag `project` da linha 10 e a descrição da linha 11:

```xml
<project name="pitiupi.ping" default="default" basedir=".">
    <description>Builds, tests, and runs the project pitiupi.ping.</description>
```

`plugins/ping/nbproject/project.properties`:

| Propriedade | Valor |
| --- | --- |
| `application.title` | `pitiupi.ping` |
| `dist.jar` | `${dist.dir}/pitiupi.ping.jar` |
| `dist.jlink.output` | `${dist.jlink.dir}/pitiupi.ping` |
| `jlink.launcher.name` | `pitiupi.ping` |

Não mexa nestas duas, no final do mesmo `project.properties`. Elas dizem ao Ant onde está o JAR do núcleo:

```
project.pitiupi=../../pitiupi
reference.pitiupi.jar=${project.pitiupi}/dist/pitiupi.jar
```

E o classpath de compilação continua:

```
javac.classpath=\
    ${reference.pitiupi.jar}
```

Sem esse JAR o compilador não acha `Plugin` nem `Message`. Gere-o antes, na raiz do clone: `cd pitiupi && ant jar`.

Apague também o SPI do chat, se a cópia ainda tiver `src/META-INF/services/pitiupi.models.Plugin` com a linha `pitiupi.chat.plugin.ChatPlugin`. O passo 2 cria o arquivo certo.

## Passo 2 — Arquivo SPI

O núcleo não dá `new PingPlugin()`. Na subida, `PluginManager` chama `ServiceLoader.load(Plugin.class, classLoader)` para cada JAR. O `ServiceLoader` procura um arquivo de texto cujo **nome** é a classe que ele deve implementar:

```
src/META-INF/services/pitiupi.models.Plugin
```

Crie as pastas `src/META-INF/services/` e grave o arquivo **sem extensão extra**, com uma linha só:

```
pitiupi.ping.plugin.PingPlugin
```

O nome do arquivo tem de ser `pitiupi.models.Plugin` porque é essa a classe abstrata do núcleo (`pitiupi.models.Plugin`). A linha tem de ser o pacote mais o nome da classe que você vai escrever no passo 6. Se o arquivo se chamar `Plugin` ou a linha for `PingPlugin` sem pacote, o JAR compila e o programa não mostra o plugin.

O Ant inclui `src/META-INF` no JAR por causa de `meta.inf.dir=${src.dir}/META-INF` no `project.properties`. Depois do `ant jar`:

```bash
jar tf dist/pitiupi.ping.jar | grep META-INF/services
```

A saída esperada é `META-INF/services/pitiupi.models.Plugin`.

## Passo 3 — Mensagens

Todas estendem `pitiupi.models.Messages.Message`, que já é `Serializable`. O construtor vazio existe porque a desserialização Java precisa criar o objeto e depois preencher os campos. Sem ele, o outro lado não reconstrói a mensagem.

`PingMode` não é mensagem. É o enum que diz se aquele ping é único ou um tique da série contínua. Arquivo `src/pitiupi/ping/net/PingMode.java`:

```java
package pitiupi.ping.net;

public enum PingMode {
    ONCE,
    CONTINUOUS_TICK
}
```

`src/pitiupi/ping/net/PingMessage.java` — a ida:

| Campo | Por que existe |
| --- | --- |
| `pingId` | Casa o pong com o ping que você enviou |
| `sentAtMillis` | Instante local de envio; o pong devolve este número |
| `senderName`, `senderIp` | A aba de quem recebe mostra quem mandou |
| `mode` | `ONCE` ou `CONTINUOUS_TICK` |
| `sessionId` | Igual em todos os tiques de um ping contínuo; `null` no envio único |

```java
package pitiupi.ping.net;

import pitiupi.models.Messages.Message;

public class PingMessage extends Message {

    private String pingId;
    private long sentAtMillis;
    private String senderName;
    private String senderIp;
    private PingMode mode;
    private String sessionId;

    public PingMessage() {
    }

    public PingMessage(
            String pingId,
            long sentAtMillis,
            String senderName,
            String senderIp,
            PingMode mode,
            String sessionId
    ) {
        this.pingId = pingId;
        this.sentAtMillis = sentAtMillis;
        this.senderName = senderName;
        this.senderIp = senderIp;
        this.mode = mode;
        this.sessionId = sessionId;
    }

    public String getPingId() { return pingId; }
    public long getSentAtMillis() { return sentAtMillis; }
    public String getSenderName() { return senderName; }
    public String getSenderIp() { return senderIp; }
    public PingMode getMode() { return mode; }
    public String getSessionId() { return sessionId; }
}
```

Inclua os setters se for seguir o arquivo do repositório; a desserialização padrão preenche os campos privados direto e o setter não é obrigatório para a rede.

`src/pitiupi/ping/net/PongMessage.java` — a volta. `echoSentAtMillis` é cópia de `sentAtMillis` do ping. Quem enviou calcula `agora - sentAt` guardado localmente; o eco existe para conferir que o pong responde àquele envio.

| Campo | Por que existe |
| --- | --- |
| `pingId` | O mesmo id do ping |
| `echoSentAtMillis` | Cópia do instante original |
| `responderName`, `responderIp` | Quem respondeu |

Construtor vazio mais o construtor `(pingId, echoSentAtMillis, responderName, responderIp)` e os getters. A classe também estende `Message`.

`src/pitiupi/ping/net/PingStopMessage.java` — só quando o contínuo para:

| Campo | Por que existe |
| --- | --- |
| `sessionId` | Qual série parou |
| `senderName`, `senderIp` | Quem parou, para a aba de recebimento |

Essas três classes entram em `PingPlugin.getMessages()` no passo 6. Se faltar uma, o núcleo descarta o byte: o outro lado “não recebe”.

## Passo 4 — Controller (enviar e receber)

`PingController` não cria janela. Ele usa o `ConnectionManager` que o núcleo entregou e o `MessageDispatcher` para não abrir socket.

Crie `src/pitiupi/ping/control/PingLogSink.java` para a tela plugar o log:

```java
package pitiupi.ping.control;

public interface PingLogSink {
    void appendLine(String tabKey, String line);
    void ensureInboundTab(String peerIp, String displayName);
}
```

`src/pitiupi/ping/control/PingController.java` guarda:

- `pendingByPingId` — `pingId` → `sentAtMillis`, para achar o RTT quando o pong chegar.
- `logTabByPingId` — em qual aba escrever esse RTT.
- `continuousTimers` — um `javax.swing.Timer` de 1000 ms por IP.
- `continuousSessionByPeer` — o `sessionId` da série daquele IP.

Constante: `CONTINUOUS_INTERVAL_MS = 1000`. Prefixo da aba que só recebe: `INBOUND_VIEW_PREFIX = "view:"`.

### Enviar um ping para um IP

`sendPing` faz, nesta ordem:

1. `buildPing` cria `PingMessage` com `UUID.randomUUID()`, `System.currentTimeMillis()`, `connectionManager.getUserName()`, `LocalNetworkInfo.bestLocalIpv4()`, o `PingMode` e o `sessionId`.
2. Guarda o instante em `pendingByPingId` e a aba em `logTabByPingId`.
3. Envia **sempre** em UDP unicast para aquele IP:

```java
MessageDispatcher.send(
        connectionManager,
        message,
        SendTarget.singlePeer(addr),
        TransportMode.UNICAST_UDP
);
```

`SendTarget.singlePeer` é um endereço. `TransportMode.UNICAST_UDP` pede ao núcleo `sendUdpUnicast`. Não use TCP aqui: o exercício é o datagrama.

`PeerIpv4.normalize` deixa o IP em IPv4 pontilhado antes de `InetAddress.getByName`.

### Enviar um ping para a sala

`pingOnceRoom` usa `SendTarget.broadcastAll()` e o transporte que o painel escolheu (`MULTICAST_UDP` ou `UNICAST_UDP`). O núcleo, no unicast para a sala, manda uma cópia para cada pessoa online. O log vai na aba `PluginSessionKeys.ROOM`.

`pingOnce(PluginRoutingPanel.RoutingSnapshot)` olha `routing.everyoneInRoom()`. Se for a sala, chama `pingOnceRoom`. Se for um IP, chama `sendPing` com `PingMode.ONCE` e `sessionId` nulo. A aba de log desse IP é `PluginSessionKeys.peer(ip)` (a chave começa com `peer:`).

### Ping contínuo

`startContinuousForPeerIp`:

1. Para um timer anterior desse IP, se houver.
2. Gera um `sessionId`. O mesmo id vai em todos os tiques, para o outro lado saber que a série é uma só.
3. Cria `new Timer(1000, e -> sendPing(..., PingMode.CONTINUOUS_TICK, sessionId, aba))` e dá `timer.start()`.

O `javax.swing.Timer` fica no controller, não no painel. O intervalo de 1 segundo é regra do ping, e a tela só liga ou desliga. Esse timer dispara na thread da interface, então a linha de log pode ir direto ao `PingLogSink` sem outro `invokeLater`.

`stopContinuousForPeerIp` para o timer e, se havia `sessionId`, manda `PingStopMessage` com o mesmo `MessageDispatcher.send(..., UNICAST_UDP)`. Sem essa mensagem, o outro lado continua mostrando a aba `↓` como se a série seguisse.

`stopAllContinuous` percorre os IPs. O plugin chama isso quando a aba central fecha.

### Receber

`onPingReceived`:

1. `logSink.ensureInboundTab(ipDeQuemEnviou, nome)` abre a aba `view:<ip>` se ela não existir.
2. Escreve uma linha nessa aba.
3. Monta `PongMessage` com o `pingId`, o `sentAtMillis` **do ping recebido** (não o relógio de agora), o seu nome e `LocalNetworkInfo.bestLocalIpv4()`.
4. Devolve em unicast para o IP de quem enviou.

O RTT não usa o relógio de quem responde. Os dois computadores não estão sincronizados. Quem enviou guardou o próprio `System.currentTimeMillis()` em `pendingByPingId` e, quando o pong volta com o mesmo `pingId`, subtrai esse valor do relógio local. O `echoSentAtMillis` só confirma que o pong responde àquele envio.

`onPongReceived`:

1. `pendingByPingId.remove(pingId)`. Se não havia entrada, ignore (pong atrasado ou de outro envio).
2. `rtt = agora - sentAt` guardado, nunca negativo.
3. Linha na aba que enviou: `pong ← Nome · RTT 42 ms`.

`onStopReceived` abre a aba `view:<ip>` e escreve que a pessoa parou o contínuo.

O controller não chama `receiveMessage`. Quem recebe do núcleo é o `PingPlugin`, no passo 6, e ele repassa para estes três métodos.

## Passo 5 — Painel

`PingPanel` estende `JPanel` e implementa `PingLogSink`. A janela do programa já é a `MainShellFrame`. `bindShellWorkspaceContent` coloca este painel dentro da aba. Um `JFrame` abriria outra janela por cima da shell, com login e lista duplicados. Não chame `setVisible` numa janela nova.

O núcleo já desenha destino, transporte e abas. Reutilizar isso evita um combo que manda multicast para um IP só, ou unicast para a sala inteira sem passar pelo `MessageDispatcher`.

- `PluginRoutingPanel` com `PluginTransportCatalog.PluginKind.PING`. Esse valor, no núcleo, lista multicast e unicast na sala, e só UDP unicast para um IP. O ping não tem TCP: o enum esconde essa opção. O terceiro argumento `false` esconde também a conexão privada (o chat usa `true` no texto privado).
- `PluginSessionWorkspace` recebe o painel de roteamento e a barra de botões. Ele empilha o combo em cima e as abas no meio.
- `SessionTabBar` é `workspace.getSessionTabBar()`.

Na construção:

```java
this.routingPanel = new PluginRoutingPanel(
        PluginTransportCatalog.PluginKind.PING,
        "<html><i>Abas <b>↑</b> enviam; <b>↓</b> só recebem. Use + Nova ou a lista lateral para abrir um peer.</i></html>",
        false
);
controller.setLogSink(this);
this.workspace = new PluginSessionWorkspace(routingPanel, buildPingToolbar());
```

`setLogSink` liga o controller à tela. Sem isso, o RTT é calculado e não aparece.

Barra (`buildPingToolbar`):

- `JButton` **Enviar ping** chama `controller.pingOnce(routingPanel.snapshot())`. `snapshot()` copia destino e transporte **neste clique**. Se o controller lesse o combo depois, um segundo clique ou uma troca de aba mudaria o envio que já estava decidido.
- `JToggleButton` **Ping contínuo**. Só age se a aba selecionada for `peer:<ip>` (envio para uma pessoa). Ligado chama `startContinuousForPeerIp`. Desligado chama `stopContinuousForPeerIp`. Na sala e nas abas `view:` o botão fica desabilitado (`applyToolbarForTab`). A aba `view:` é de quem recebeu: um ping contínuo dali responderia para dentro da própria recepção e misturaria as duas direções.

Abas. A chave é uma `String` que o `SessionTabBar` usa para não abrir a mesma conversa duas vezes.

| Chave | Título | Por que essa chave |
| --- | --- | --- |
| `PluginSessionKeys.ROOM` | `Todos ↑` (`SessionTabDirectionLabels.outboundTitle`) | A sala já tem nome no núcleo (`"room"`). Não fecha: é o fallback quando as outras fecham |
| `peer:<ip>` | `Nome ↑` | Você escolheu o destino. Nasce na lista lateral, no **+ Nova** ou no rascunho de IP |
| `view:<ip>` | `Nome ↓` | Convenção do plugin, não do núcleo. Nasce em `ensureInboundTab` quando chega um ping. `bindReceiveSession` deixa IP e transporte somente leitura |

`focusMulticastSession()` seleciona a aba da sala. `focusPeerAddress(UserPeerView)` abre `peer:<ip>` com `PeerIpv4.fromInetAddress`. As peer actions do passo 6 chamam esses dois métodos e não disparam ping: a ação da lista só escolhe com quem falar. O datagrama continua no botão, senão um clique no nome já mediria RTT sem a pessoa confirmar.

`appendLine` escreve `[HH:mm:ss] ` mais o texto num `JTextArea` monoespaçado daquela chave. Uma área por aba, dentro de um `CardLayout`: o card mostra só a aba selecionada e as outras permanecem na memória. Sem o card, trocar de aba apagaria o histórico ou empilharia todos os textos ao mesmo tempo.

`PeerDraftPanel` no **+ Nova** pede um IP e vira uma aba `peer:`. É o mesmo componente do núcleo que o chat usa para IP que ainda não está na lista.

O contrato com o controller é `PingLogSink` mais `pingOnce`, `startContinuousForPeerIp` e `stopContinuousForPeerIp`. O arquivo `plugins/ping/src/pitiupi/ping/GUI/PingPanel.java` é essa montagem pronta. Os métodos acima são os que mudam o comportamento; o resto é borda, fonte e rótulo.

## Passo 6 — PingPlugin

Arquivo `src/pitiupi/ping/plugin/PingPlugin.java`. A classe estende `pitiupi.models.Plugin`. O nome na linha do SPI tem de ser exatamente `pitiupi.ping.plugin.PingPlugin`.

Identidade:

| Método | Retorno |
| --- | --- |
| `getName()` | `Ping` |
| `getDisplayName()` | `Ping na rede` |
| `getAuthor()` | o seu nome ou `Aula Pitiupi` |
| `getVersion()` | `1.0.0` |
| `getIdentifier()` | `pitiupi.ping` |

`getIdentifier()` é o id que o núcleo usa se um dia houver TCP dirigido a este plugin. Tem de ser único na sala.

`createPlugin` guarda o `PluginWorkspace`, cria o controller com o `ConnectionManager`, cria o painel, registra o painel e as ações. Não chame `openOrFocusTab` aqui: a aba não abre sozinha no login.

```java
@Override
public void createPlugin(ConnectionManager connectionManager, PluginWorkspace workspace) {
    this.workspace = workspace;
    this.controller = new PingController(connectionManager);
    this.panel = new PingPanel(connectionManager, controller);
    bindShellWorkspaceContent(panel);
    installPeerActions(workspace.peerActions());
}
```

`bindShellWorkspaceContent` é método da classe `Plugin`. Sem ele a galeria abre uma aba vazia.

`getMessages` declara os três tipos. É esta lista que autoriza a desserialização:

```java
@Override
public List<Class<? extends Message>> getMessages() {
    return List.of(PingMessage.class, PongMessage.class, PingStopMessage.class);
}
```

`receiveMessage` não mexe na tela direto. O núcleo pode chamar esse método na thread que leu o socket. `JButton` e `JTextArea` só aceitam mudança na thread do Swing; fora dela a interface trava ou desenha pela metade. `SwingUtilities.invokeLater` joga o `instanceof` e a chamada ao controller para essa thread:

```java
@Override
public void receiveMessage(Message message) {
    SwingUtilities.invokeLater(() -> {
        if (message instanceof PingMessage ping) {
            controller.onPingReceived(ping);
        } else if (message instanceof PongMessage pong) {
            controller.onPongReceived(pong);
        } else if (message instanceof PingStopMessage stop) {
            controller.onStopReceived(stop);
        }
    });
}
```

`onShellTabClosed` chama `controller.stopAllContinuous()` para o timer não continuar com a aba fechada.

### Peer actions

Registre em `installPeerActions`. As duas só abrem a interface. O datagrama sai do botão.

| id | Rótulo | Alvo | O que faz |
| --- | --- | --- | --- |
| `ping.room` | Abrir ping na sala (Todos) | `PeerActionTarget.ALL_PEERS` | `openOrFocusTab` e `panel.focusMulticastSession()` |
| `ping.peer` | Enviar ping para esta pessoa | `PeerActionTarget.SINGLE_PEER` | `openOrFocusTab` e, se o alvo for `PeerTarget.Single`, `panel.focusPeerAddress(single.peer())` |

```java
registrar.register(PeerAction.of(
        "ping.peer",
        "Enviar ping para esta pessoa",
        PeerActionTarget.SINGLE_PEER,
        target -> {
            workspace.openOrFocusTab(getIdentifier(), getDisplayName());
            if (target instanceof PeerTarget.Single single) {
                panel.focusPeerAddress(single.peer());
            }
        }
));
```

A ação de sala é a mesma forma, com `PeerActionTarget.ALL_PEERS` e `focusMulticastSession()`. O id (`ping.peer`, `ping.room`) só precisa ser único dentro do plugin.

## Passo 7 — Catálogo, compilar, instalar

Na raiz do clone, `plugins.registry.json` precisa de uma entrada para o script compilar e vincular o módulo:

```json
{
  "id": "ping",
  "dir": "plugins/ping",
  "build": "ant",
  "jar": "dist/pitiupi.ping.jar",
  "menuCategory": "Social"
}
```

`id` vira o arquivo `.pitiupi/plugins/ping.jar`. `dir` + `jar` acham `plugins/ping/dist/pitiupi.ping.jar`. `build: ant` manda rodar `ant jar` nessa pasta. `menuCategory` só filtra `./scripts/pitiupi.sh run --category Social`. Não cria menu na janela.

Compile, na pasta `plugins/ping`:

```bash
ant jar
jar tf dist/pitiupi.ping.jar | grep META-INF/services
```

Depois, na raiz:

```bash
./scripts/pitiupi.sh link --all
```

Ou instale `dist/pitiupi.ping.jar` em **Configurações → Instalar JAR…**. Isso copia o arquivo e grava uma entrada `build: jar` no catálogo. Reinicie o Pitiupi nos dois casos. O processo aberto não relê a pasta de plugins.

## Passo 8 — Ver o RTT

1. Duas janelas no mesmo PC: portas **5000** e **5001**, nomes diferentes. Elas não ficam na mesma sala (a sala é a porta). Servem para ver se o JAR carregou: a galeria mostra **Ping na rede**.
2. Dois computadores, mesma porta (5000), mesmos JARs: a lista mostra a outra pessoa. Clique nela → **Enviar ping para esta pessoa**. A aba `↑` abre com o IP. Clique **Enviar ping**. A linha do RTT aparece na aba de quem enviou; a outra ganha uma aba `↓`.
3. Na aba `↑`, ligue **Ping contínuo**. Sai um ping por segundo, com o mesmo `sessionId`. Desligue: o outro vê “parou o ping contínuo” na aba `↓`.
4. **Abrir ping na sala (Todos)** foca a aba da sala e não envia. O envio é o botão, com multicast ou unicast no combo.

Se a lista só mostra **Todos**, o problema é rede (porta, sub-rede, firewall, WSL), não o painel. Veja [usar.md](../usar.md).

## Quando algo não acontece

| O que você vê | O que conferir |
| --- | --- |
| O plugin não aparece na galeria | A linha do SPI, o `jar tf`, e se o JAR está em `.pitiupi/plugins` depois do reinício |
| O peer não recebe | A classe está em `getMessages()`? Os dois usam o mesmo JAR? UDP na porta do login está liberado? |
| O RTT não aparece | O `pingId` do pong não está em `pendingByPingId` (id diferente, ou o pong chegou antes de você guardar o mapa) |
| A aba abre vazia | Faltou `bindShellWorkspaceContent(panel)` |
| A ação da lista manda ping sozinha | A peer action deve só focar a aba; o `MessageDispatcher.send` fica no botão |
| `ClassCastException` ou a mensagem some | Versões diferentes da classe nos dois PCs |
| O contínuo continua com a aba fechada | `onShellTabClosed` não chamou `stopAllContinuous` |
