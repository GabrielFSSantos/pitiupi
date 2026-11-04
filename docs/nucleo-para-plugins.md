# Funções do núcleo para um plugin

Um plugin é uma classe que estende `pitiupi.models.Plugin`. O núcleo cria a instância, chama `createPlugin` e, quando chega um datagrama ou um frame TCP do tipo que você declarou, chama `receiveMessage`. Você não abre `MulticastSocket` nem `ServerSocket`.

A classe base está em `pitiupi/src/pitiupi/models/Plugin.java`.

## O que implementar

| Método | Para que serve |
| --- | --- |
| `getName()`, `getAuthor()`, `getVersion()` | Texto de identificação do módulo |
| `getIdentifier()` | Id estável, único na sala. O núcleo usa esse texto para achar o plugin numa conexão TCP dirigida a ele. Exemplo do ping: `pitiupi.ping` |
| `getDisplayName()` | Nome na galeria e na aba. Se você não sobrescrever, vale `getName()` |
| `createPlugin(ConnectionManager, PluginWorkspace)` | Único lugar de arranque. Aqui você cria o painel, chama `bindShellWorkspaceContent` e registra as ações da lista |
| `getMessages()` | Lista das classes `Message` deste plugin. Sem a classe aqui, o núcleo não entrega o objeto |
| `receiveMessage(Message)` | Chegou uma mensagem cuja classe está na lista. Trate na thread do Swing com `SwingUtilities.invokeLater` se for mexer em botão ou texto |

`createPlugin` **não** abre a aba. A aba abre quando a pessoa clica na galeria ou numa peer action.

## O que é opcional

| Método | Quando usar |
| --- | --- |
| `onShellTabClosed()` | A pessoa fechou a aba do plugin no topo. Pare timer, captura de tela ou envio contínuo |
| `receiveAllTypes()` | Receber toda mensagem pública, não só as suas. O padrão é `false` |
| `receivePrivateConnection(TcpConnection)` | Aceitar um TCP aberto para o seu `getIdentifier()` |
| `receiveBytes(TcpConnection, byte[])` | Ler bytes crus nessa conexão, em vez de uma `Message` |
| `onSocketClosed(TcpConnection)` | Limpar estado quando o socket fecha |
| `onInviteAccepted(InviteDetails)` | Reagir a um convite aceito |

`bindShellWorkspaceContent(JComponent)` é `protected` na classe `Plugin`. Guarda o painel que a shell mostra. Não use `JFrame`: a janela já é a `MainShellFrame`.

## Enviar

`MessageDispatcher.send` devolve quantos envios o núcleo tentou.

```java
MessageDispatcher.send(
        connectionManager,
        mensagem,
        SendTarget.singlePeer(endereco),
        TransportMode.UNICAST_UDP
);
```

`SendTarget` é o destino:

- `SendTarget.broadcastAll()` — a sala (a linha **Todos**).
- `SendTarget.singlePeer(InetAddress)` — um IPv4.

`TransportMode` é o meio:

| Valor | O que o núcleo faz |
| --- | --- |
| `MULTICAST_UDP` | Um datagrama para `224.0.0.3` |
| `UNICAST_UDP` | Um datagrama para o IP. Se o destino for a sala inteira, o núcleo manda uma cópia para cada pessoa online |
| `TCP` | Frame tamanho + bytes. Na sala inteira, uma conexão por pessoa |

`describeForUi(boolean)` é o texto do combo (“Direto para o IP da pessoa (UDP unicast)”). O plugin de referência usa `PluginRoutingPanel` do núcleo para esse combo, em vez de desenhar o seletor do zero.

`ConnectionManager` que chega em `createPlugin` também expõe `getUserName()`, `sendUdpUnicast`, `sendUdpMulticast` e `sendTcp`. Para o caso comum, `MessageDispatcher` já escolhe o método certo.

IP legível na interface: `PeerIpv4.normalize(...)` e `LocalNetworkInfo.bestLocalIpv4()`. No Windows, `InetAddress.getHostAddress()` pode vir com `::ffff:` ou sufixo `%n`.

## Aba e lista de pessoas

`PluginWorkspace` (`pitiupi.api.plugin.PluginWorkspace`):

- `connectionManager()` — o mesmo gestor de rede.
- `peerActions()` — onde você registra ações.
- `openOrFocusTab(pluginId, titulo)` — abre ou foca a aba central. As peer actions chamam isso; `createPlugin` não.
- `setStatusMessage(String)` — texto de status da shell.

Uma peer action é um `PeerAction.of(id, rótulo, alvo, o que fazer)`:

- `PeerActionTarget.SINGLE_PEER` — aparece ao clicar numa pessoa. O alvo chega como `PeerTarget.Single` e `single.peer()` é a ficha dessa pessoa (nome e IP).
- `PeerActionTarget.ALL_PEERS` — aparece na linha **Todos**.

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

A lista agrupa as ações de todos os plugins, num submenu por `getDisplayName()`. A ação do ping de referência só abre a aba e o IP. O envio fica no botão do painel.

## Mensagem sua

Estenda `pitiupi.models.Messages.Message`. Construtor sem argumentos é necessário para a desserialização Java. Campos precisam ser serializáveis (`String`, `long`, enum, etc.).

```java
public class PingMessage extends Message {
    private String pingId;
    private long sentAtMillis;

    public PingMessage() {
    }
}
```

Registre a classe:

```java
@Override
public List<Class<? extends Message>> getMessages() {
    return List.of(PingMessage.class, PongMessage.class);
}
```

O núcleo só entrega essas classes para este plugin. Outro JAR não recebe o objeto por engano, e um tipo que você esqueceu de listar é descartado.

Em `receiveMessage`, separe com `instanceof` e passe o trabalho para uma classe sem Swing (no ping, `PingController`). A atualização da tela volta pelo `invokeLater`: o núcleo pode chamar `receiveMessage` fora da thread do Swing, e botão ou texto só mudam nessa thread.

`Message` tem `isPublic()`. O padrão do construtor é mensagem pública (a sala e o despacho normal). Uma conexão TCP privada não passa por essa lista: ela usa os métodos da seção seguinte.

## TCP dirigido a um plugin

Serve quando o fluxo não é uma `Message` da sala, e sim bytes numa conexão só sua. O chat faz isso para o texto privado.

1. Quem inicia chama `connectionManager.connectPluginTcpAsync(ip, getIdentifier(), getIdentifier(), listener)`. Os dois ids são o mesmo `getIdentifier()`: o destinatário no handshake é este plugin, nos dois lados. O `TcpAttemptListener` recebe “conectando”, sucesso ou falha. O prazo de 5 segundos e o aviso na janela estão em [rede.md](rede.md).
2. No outro lado, `receivePrivateConnection` devolve `true` para aceitar. `false` faz o núcleo responder que o plugin não está lá.
3. Cada frame seguinte chega em `receiveBytes(TcpConnection, byte[])`. O plugin interpreta os bytes. O chat desserializa a mensagem antiga do texto privado.
4. `onSocketClosed` limpa o estado quando o socket fecha.

Não use esse caminho para um ping ou um texto de sala. `MessageDispatcher.send(..., TransportMode.TCP)` já manda uma `Message` na conexão pública.

## Aviso de sessão

`SessionNoticeMessage` avisa que uma atividade começou ou terminou, sem ser o payload em si. Campos: id do plugin, fase `START` ou `END`, canal, nome e IP de quem enviou, escopo `BROADCAST` ou `DIRECT`, e o `TransportMode` usado.

Inclua `SessionNoticeMessage.class` em `getMessages()` se quiser reagir. O screencast envia fase `END` e canal `SCREEN_CAST` quando a pessoa para o compartilhamento. Quem está na aba de recebimento fecha essa aba. O canal hoje só tem `SCREEN_CAST`; outro canal pede um valor novo no enum do núcleo.

## Abas de sessão

O núcleo oferece as chaves em `PluginSessionKeys`:

| Chave | Significado |
| --- | --- |
| `ROOM` (`"room"`) | A sala, a linha **Todos** |
| `peer(ip)` (`"peer:"` + IP) | Aba de envio para aquele IP |
| `PEER_DRAFT` | Rascunho do **+ Nova**, antes do IP ser confirmado |

A chave `view:` + IP não é do núcleo. É convenção dos plugins de referência para a aba que nasceu porque chegou tráfego, com envio desligado (`PluginRoutingPanel.bindReceiveSession`).

`routingPanel.snapshot()` lê destino e transporte no instante do clique. O controller guarda esse retrato e não consulta o combo depois. Assim a regra de envio não depende do componente Swing.

## Convite e selo

`onInviteAccepted(InviteDetails)` existe. O núcleo entrega o convite se alguém enviar `SessionInviteMessage`. Chat, screencast e ping não enviam. A janela não mostra um diálogo de convite.

`PeerActivityInbox` conta atividade por pessoa. A lista lateral não desenha um número. O que a pessoa vê é a aba do plugin.

## O que o plugin não faz

- Não escolhe a porta nem o grupo multicast. Isso é login e Configurações.
- Não mantém a lista de presença. Leia as pessoas por `connectionManager.getPresenceService().listPeerViews()` se precisar; a shell já mostra a lista.
- Não recompila o núcleo. O classpath de compilação do plugin aponta para `pitiupi/dist/pitiupi.jar`.
