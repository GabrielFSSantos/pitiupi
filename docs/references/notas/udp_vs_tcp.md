# UDP vs TCP (nota)

| | UDP | TCP |
| --- | --- | --- |
| Conexão | Não | Sim |
| Confiabilidade | Melhor esforço | Entrega ordenada na conexão |
| Delimitação de mensagem | Por datagrama | Fluxo de bytes — Pitiupi usa framing 4+N |
| Uso no Pitiupi | Heartbeat, multicast, plugins | Canais diretos peer-a-peer |

Ver [05_udp_multicast.md](../../documentation/05_udp_multicast.md) e [06_tcp_conexoes.md](../../documentation/06_tcp_conexoes.md).
