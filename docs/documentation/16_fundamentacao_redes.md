# Fundamentação — redes e código

Mapa entre conceitos de **Redes de Computadores** e o Pitiupi. Aprofundamento: [references/README.md](../references/README.md).

## Camada de aplicação P2P

| Conceito | No Pitiupi |
| --- | --- |
| Aplicação distribuída sem servidor dedicado | Cada peer executa o mesmo núcleo + plugins |
| Identidade do participante | Nome no login + `HeartbeatMessage` (instância) |
| Broadcast lógico | UDP multicast no grupo `224.0.0.3` |
| Comunicação 1:1 | UDP unicast ou TCP com conexão persistente |

## UDP

- Sem conexão; mensagens delimitadas por datagrama.
- Multicast economiza envios para todos os peers do grupo.
- Limitações: perda, ordem, MTU — relevante para plugins com imagens (screenCast).

Doc: [05_udp_multicast.md](05_udp_multicast.md).

## TCP

- Conexão orientada a fluxo; o núcleo adiciona **framing** (tamanho + payload).
- Uma conexão por endereço de peer reutilizada para várias mensagens.
- Fechamento quando peer some do heartbeat.

Doc: [06_tcp_conexoes.md](06_tcp_conexoes.md).

## Presença

Heartbeats periódicos em UDP; timeout ~15 s. Analogia com “keepalive” de presença, não com camada de transporte confiável.

Doc: [08_heartbeat_e_presenca.md](08_heartbeat_e_presenca.md).

## Serialização

Objetos Java `Serializable` em `byte[]` — acoplamento de versão de classes entre peers (mesmo plugin instalado).

Doc: [07_mensagens_e_serializacao.md](07_mensagens_e_serializacao.md).

## Extensibilidade

SPI e classloader por JAR — padrão de plug-in em aplicações Java.

Doc: [09_sistema_de_plugins.md](09_sistema_de_plugins.md).

## Quando o Pitiupi usa cada transporte

| Situação | Transporte típico | Motivo |
| --- | --- | --- |
| Lista de usuários / heartbeat | UDP multicast | Todos os peers sem conexão prévia |
| Sala geral (chat, broadcast) | UDP multicast ou cópias unicast/TCP conforme combo | Escolha explícita na UI do plugin |
| Mensagem para um IP | UDP unicast ou TCP | Destino explícito |
| Fluxo contínuo (tela, TCP privado chat) | TCP | Fluxo e framing no núcleo |

UDP não é “sempre mais rápido” nem TCP “resolve tudo”: perda e ordem ainda importam na aplicação; TCP adiciona handshake e estado por peer.

## Leitura complementar

- [references/notas/udp_vs_tcp.md](../references/notas/udp_vs_tcp.md)
- [references/README.md](../references/README.md) (bibliografia)
- [references/notas/multicast_lan.md](../references/notas/multicast_lan.md)
- [references/notas/java_spi_plugins.md](../references/notas/java_spi_plugins.md)
