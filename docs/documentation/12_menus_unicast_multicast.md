# Menus, peer actions e transportes

## Interface atual (shell v1)

Plugins de referência **não** dependem da barra de menus superior para o fluxo principal:

- **Galeria** na aba **Plugins** abre cada extensão em aba própria (fechável com ×).
- **Peer actions** na lista **Usuários nesta rede** (clique em um peer ou em **Todos**) agrupam ações de todos os plugins instalados.
- Dentro da aba do plugin, **`PluginRoutingPanel`** padroniza destino (sala geral vs IP), forma de envio (multicast UDP, UDP unicast, TCP) e texto de ajuda.

Ver [04_inicializacao_e_gui.md](04_inicializacao_e_gui.md) e [09_sistema_de_plugins.md](09_sistema_de_plugins.md).

## MenuCategory (legado)

Enum em `pitiupi.models.Menus.MenuCategory` (`Jogos`, `Social`, `Streaming`) ainda aparece no **catálogo** (`menuCategory` em `plugins.registry.json`) e em plugins antigos que chamam `GuiManager.createMenu(...)`.

Novos plugins devem preferir `PluginWorkspace` + peer actions. O menu da shell expõe apenas **Ajuda → Sobre**.

## Unicast vs multicast na aplicação

| Modo | API típica | Semântica |
| --- | --- | --- |
| Multicast | `MessageDispatcher` / `SocketUDP.sendMulticast` | Todos no grupo na porta configurada |
| Unicast UDP | envio para IP de um peer | Um destinatário, sem estabelecer TCP |
| TCP | `ConnectionManager` / `SocketTCP` | Canal com framing 4+N bytes |

Detalhes de rede: [05_udp_multicast.md](05_udp_multicast.md), [06_tcp_conexoes.md](06_tcp_conexoes.md).

## Referência de código

- Chat e screencast: peer actions em `ChatPlugin` e `ScreenCastPlugin`.
- Envio unificado: `pitiupi.application.messaging.MessageDispatcher` e `TransportMode`.

O antigo plugin **mpChat** foi unificado no módulo `plugins/chat` (mensagens `LegacyMpChatMessage` para compatibilidade TCP privado). Ver [11_plugins_referencia.md](11_plugins_referencia.md) e decisão D19 no [decision-register.md](../decision-register.md).
