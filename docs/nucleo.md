# Mapa do núcleo

O código do programa está em `pitiupi/src/pitiupi`. Um plugin não precisa abrir essas pastas para funcionar: o contrato de chamada está em [nucleo-para-plugins.md](nucleo-para-plugins.md). Este capítulo é para quem vai ler o código ou alterar o núcleo.

## Pastas

| Pacote | O que faz |
| --- | --- |
| `Main` | Garante `.pitiupi/`, pede nome e porta no `LoginDialog` e abre `MainShellFrame` |
| `control.ConnectionManager` | Fachada da rede: porta, nome, envio UDP e TCP, presença, lista de ações |
| `control.HeartbeatManager` | Anúncio periódico e timeout de quem sumiu |
| `control.plugins.PluginManager` | Lê os JARs, acha as classes pelo SPI e entrega `receiveMessage` |
| `net.udp.SocketUDP` | O `MulticastSocket` do grupo `224.0.0.3` |
| `net.tcp` | `ServerSocket`, uma `TcpConnection` por IP, framing de 4 bytes + N bytes |
| `application.messaging` | `MessageDispatcher`, `SendTarget`, `TransportMode`, combo de transporte |
| `application.presence` | A lista **Usuários nesta rede** (`PresenceService`, `UserPeerView`) |
| `application.peeractions` | O menu que aparece ao clicar numa pessoa |
| `application.session` | Aviso de início ou fim de uma atividade (`SessionNoticeMessage`) |
| `application.invites` | Convite de sessão. O núcleo recebe e pode chamar `onInviteAccepted`. Nenhum plugin de referência envia convite, e a janela não mostra uma caixa de convite |
| `application.activity` | Contagem de atividade por pessoa. A lista lateral não desenha selo |
| `presentation.shell` | Janela, abas, galeria, configurações, painéis que o plugin reutiliza |
| `models` | `Plugin`, `Message` e as mensagens do próprio núcleo |
| `plugins.store` | Copia o JAR para `.pitiupi/plugins` e, no clone, grava `plugins.registry.json` |
| `platform` | Onde fica `.pitiupi` (`PitiupiHome`) e `config.properties` de layout |
| `errors.ErrorCode` | Quatro textos em português: falha ao instalar, ao remover, ao carregar, caminho inválido |

Nome, porta, TTL e saída de rede ficam nas preferências do Java (`NetworkPreferences`), não nesse `config.properties`.

## Caminho de um datagrama UDP

`SocketUDP.receive` lê o pacote e tenta desserializar com o classloader do núcleo.

1. `HeartbeatMessage` vai para `ConnectionManager.receiveHeartbeat`. Não chega no plugin.
2. `GoodbyeMessage` vai para `receiveGoodbye`. Não chega no plugin.
3. `SessionInviteMessage` vai para `receiveSessionInvite`. Não chega em `receiveMessage`.
4. Qualquer outro byte vai para `PluginManager.handleMessage`. Cada JAR tenta desserializar com o próprio classloader. Se a classe está em `getMessages()` (ou `receiveAllTypes()` é verdadeiro e a mensagem é pública), o núcleo chama `receiveMessage`.

Por isso heartbeat e goodbye não entram na lista do plugin. Se entrarem, o classloader do JAR não é o dono dessas classes.

## Caminho de um frame TCP

`TcpConnection` lê um `int` e depois exatamente essa quantidade de bytes. Na abertura, o núcleo troca um `HandshakeMessage` (classe interna de `net.tcp`, invisível ao plugin) com o remetente, o destinatário e a marca de conexão privada.

- Conexão pública: o frame, se for uma `Message`, segue o mesmo `PluginManager.handleMessage` do UDP.
- Conexão privada cujo destinatário é um `getIdentifier()` de plugin: `PluginManager.handleConnection` chama `receivePrivateConnection`. Se o plugin devolve `true`, os frames seguintes vão para `receiveBytes`, e o fechamento chama `onSocketClosed`.
- Destinatário que nenhum plugin aceita: o núcleo manda um `InfoMessage` de receptor não encontrado e não entrega os bytes.

O prazo de 5 segundos para abrir o socket está em [rede.md](rede.md).

## Mensagens que o núcleo define

| Classe | Quem cria | Quem consome |
| --- | --- | --- |
| `HeartbeatMessage` | `HeartbeatManager`, em multicast | `SocketUDP` → presença. Fora do plugin |
| `GoodbyeMessage` | Fechamento da janela ou troca de porta | `SocketUDP` → tira a pessoa da lista. Fora do plugin |
| `InfoMessage` | Núcleo, em conexões TCP de controle | O próprio TCP (erro, fechamento, plugin ausente). Não é texto de chat |
| `HandshakeMessage` | Abertura TCP, pacote `net.tcp` | Só o núcleo. Não declare no plugin |
| `SessionNoticeMessage` | Um plugin, em geral o screencast ao parar | Outro plugin que listar a classe em `getMessages()` |
| `SessionInviteMessage` | Quem chamar o envio | `InviteInboxService`. A interface atual não mostra o convite |

`SessionNoticeMessage` carrega o id do plugin, a fase (`START` ou `END`), o canal (`SCREEN_CAST` hoje), o nome e o IP de quem enviou, o escopo (`BROADCAST` ou `DIRECT`) e o `TransportMode`. O screencast manda `END` no canal `SCREEN_CAST` quando o compartilhamento para. Quem recebe e registrou o tipo fecha a aba de recebimento.

## Onde alterar o núcleo

| Mudança | Onde |
| --- | --- |
| Novo tipo só do seu plugin | Classe no JAR, `extends Message`, entrada em `getMessages()`. O núcleo não muda |
| Novo transporte no combo da interface | Valor em `PluginTransportCatalog.PluginKind`. Hoje existem `CHAT`, `SCREEN_CAST` e `PING` |
| Mensagem de infraestrutura (todo mundo precisa entender sem ter o JAR) | `models.Messages` e o filtro em `SocketUDP.tryParseCoreUdpMessage` |
| Nova ação na lista de pessoas | Não é mudança de núcleo: `PeerAction` no plugin |
| Texto de erro de instalação | `errors.ErrorCode` |

O grupo multicast, a porta única e o framing TCP estão decididos em [decision-register.md](decision-register.md). O comportamento na rede está em [rede.md](rede.md).
