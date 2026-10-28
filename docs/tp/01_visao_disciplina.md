# Visão da disciplina

## Papel do Pitiupi

O Pitiupi é uma **plataforma P2P com plugins** usada em Redes de Computadores para:

- Experimentar UDP (multicast/unicast) e TCP na mesma aplicação.
- Observar **presença** de peers (heartbeat) sem servidor central de aplicação.
- Relacionar conceitos de redes com código Java executável na LAN.

O núcleo concentra rede, presença e interface; extensões entram como plugins em JAR.

## Plugins de referência no repositório

| Módulo | Ideia |
| --- | --- |
| `plugins/chat` | Mensagens e vários transportes (multicast, unicast, TCP) |
| `plugins/screencast` | Payload maior / streaming de imagem |

Detalhes técnicos: [11_plugins_referencia.md](../documentation/11_plugins_referencia.md).

## Exercício em sala

O passo a passo do **Ping** (mensagens na rede, RTT, UI mínima) está em [02_tutorial_ping_aula.md](02_tutorial_ping_aula.md).

## Aprofundamento técnico

- Primeiro uso: [00_quickstart.md](../documentation/00_quickstart.md)
- Rede e protocolos no código: [16_fundamentacao_redes.md](../documentation/16_fundamentacao_redes.md) e capítulos 05–08 em [documentation/](../documentation/README.md)
