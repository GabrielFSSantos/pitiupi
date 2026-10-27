# Plugins de referência

Dois módulos buildáveis no catálogo; o chat unifica o antigo mpChat (multicast, unicast, TCP e canal TCP privado).

## Comparativo

| | plugins/chat | plugins/screencast |
| --- | --- | --- |
| Build | Ant | Ant |
| id | `chat` | `screencast` |
| Classe | `ChatPlugin` | `ScreenCastPlugin` |
| Mensagem | `ChatMessage`, `LegacyMpChatMessage` | `ScreenCastMessage` |
| UI | `ChatPanel` (destino + transporte) | `ScreenCastPanel` (destino + transporte) |
| TCP privado | Toggle no `ChatPanel` + hooks `receiveBytes` (histórico `private:<IP>`) | — |

## plugins/chat

- Sala geral ou um peer (lista + IP).
- Transportes: multicast UDP, unicast UDP, TCP via `MessageDispatcher`.
- Peer actions: abrir sala geral; conversar com peer; canal TCP privado.
- Caminho: `plugins/chat/src/pitiupi/chat/plugin/ChatPlugin.java`.

## plugins/screencast

- Compartilhamento contínuo com destino geral (multicast recomendado) ou um peer (unicast/TCP).
- Peer actions: compartilhar na rede; compartilhar com este peer.
- Caminho: `plugins/screencast/src/pitiupi/screenCast/plugin/ScreenCastPlugin.java`.

## Exercício Ping

Material em [17_tutorial_ping_aula.md](17_tutorial_ping_aula.md) e pasta `plugins/ping/` (sem JAR no repositório).
