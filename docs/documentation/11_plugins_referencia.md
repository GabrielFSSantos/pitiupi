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

Material em [17_tutorial_ping_aula.md](17_tutorial_ping_aula.md) e pasta `plugins/ping/` (sem código/JAR no repositório). Não entra no `plugins.registry.json` nem no gate CI.

## Limitações didáticas (não são bugs)

Os plugins de referência são **exemplos de sala de aula**, não produtos completos:

| Plugin | Inclui | Não inclui (de propósito) |
| --- | --- | --- |
| **chat** | texto, sala geral, unicast/multicast/TCP, TCP privado básico | imagens, moderação, histórico persistente, criptografia |
| **screencast** | stream read-only, avisos de sessão | controle remoto do mouse/teclado, áudio |
| **ping** (exercício do aluno) | chrome de sessão v1 (`PluginRoutingPanel`, abas ↑/↓), RTT, UDP apenas | TCP privado, ICMP do SO, gráficos, SLA |

Para evoluir: copie o padrão (`Plugin` + mensagens + painel na aba) e acrescente features no **seu** JAR — sem inflar o núcleo.
