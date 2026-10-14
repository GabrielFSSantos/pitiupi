# Menus — unicast e multicast

## MenuCategory

Enum em `pitiupi.models.Menus.MenuCategory`:

| Valor | Uso sugerido |
| --- | --- |
| `Jogos` | Plugins de jogos entre peers |
| `Social` | Chat, presença, mensagens sociais |
| `Streaming` | Screen cast, mídia |

## Registrar menu (padrão atual)

Via `GuiManager.createMenu(MenuCategory category, Menu menu)` — ver `ChatPlugin`:

```java
guiManager.createMenu(MenuCategory.Social, new ChatMenu(chatWindow));
```

A hierarquia de menus unicast/multicast do núcleo (`UnicastMenu`, `MulticastMenu`, etc.) permite ações por peer ou broadcast; plugins de referência encapsulam isso em classes `*Menu` próprias.

## Unicast vs multicast na aplicação

| Modo | API típica | Semântica |
| --- | --- | --- |
| Multicast | `ConnectionManager` → `SocketUDP.sendMulticast` | Todos no grupo na porta configurada |
| Unicast UDP | envio para IP de um peer | Um destinatário, sem estabelecer TCP |
| TCP | `ConnectionManager` / `SocketTCP` | Canal persistente, framing 4+N bytes |

Detalhes de rede: [05_udp_multicast.md](05_udp_multicast.md), [06_tcp_conexoes.md](06_tcp_conexoes.md).

## mpChat

`MulticastMpChatMenu` e `UnicastMpChatMenu` mostram como expor as duas modalidades no mesmo plugin — [11_plugins_referencia.md](11_plugins_referencia.md).
