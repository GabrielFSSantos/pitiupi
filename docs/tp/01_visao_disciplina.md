# Visão da disciplina

O Pitiupi é uma plataforma P2P com plugins para praticar, na mesma aplicação:

- UDP multicast e unicast, e TCP com mensagem delimitada por tamanho.
- Presença por heartbeat, sem servidor central.
- Uma extensão em JAR que usa o núcleo em vez de abrir o próprio socket.

O código do programa está descrito em [comecar.md](../comecar.md), [rede.md](../rede.md) e [plugins.md](../plugins.md). As funções que um plugin chama estão em [nucleo-para-plugins.md](../nucleo-para-plugins.md).

| Módulo no repositório | O que exercita |
| --- | --- |
| `plugins/chat` | Sala, um IP, UDP e TCP |
| `plugins/screencast` | Payload maior, multicast, unicast ou TCP |
| `plugins/ping` | RTT com ping, pong e parada em UDP |

O passo a passo para montar o ping — qual arquivo, qual linha e qual método do núcleo — está em [02_tutorial_ping_aula.md](02_tutorial_ping_aula.md). A pasta `plugins/ping` já contém essa implementação.
