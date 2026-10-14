# Guia para desenvolvedor e agente

## Leitura mínima antes de mudar código

1. [00_quickstart.md](00_quickstart.md) — build e execução
2. [04_inicializacao_e_gui.md](04_inicializacao_e_gui.md) — boot e diagrama
3. [09_sistema_de_plugins.md](09_sistema_de_plugins.md) — contrato de extensão
4. [14_arquitetura_e_manutencao.md](14_arquitetura_e_manutencao.md) — mapa de pacotes

## Por tipo de tarefa

| Tarefa | Ler primeiro |
| --- | --- |
| Novo plugin (aluno) | [10_tutorial_plugin.md](10_tutorial_plugin.md), [11_plugins_referencia.md](11_plugins_referencia.md) |
| Bug UDP / multicast | [05_udp_multicast.md](05_udp_multicast.md), [appendix_portas_e_multicast.md](appendix_portas_e_multicast.md) |
| Bug TCP / conexão | [06_tcp_conexoes.md](06_tcp_conexoes.md) |
| Presença / lista de usuários | [08_heartbeat_e_presenca.md](08_heartbeat_e_presenca.md) |
| Desserialização / mensagens | [07_mensagens_e_serializacao.md](07_mensagens_e_serializacao.md) |

## Gate de build

Compilar núcleo e os três plugins de referência:

```bash
cd pitiupi && ant jar
cd ../pitiupi.chat && ant jar
cd ../pitiupi.screenCast && ant jar
cd ../pitiupi.mpChat && make
```

Requer JDK, Ant e Make no PATH. Teste manual em rede: [15_testes_e_gate.md](15_testes_e_gate.md).

## Convenções

- Código e identificadores em inglês (pacotes `pitiupi.*`).
- Documentação em português (BR) em `docs/`.
- Evitar alterar o núcleo para necessidades de um único plugin — preferir extensão via `Plugin`.

## Decisões e débitos

- [decision-register.md](../decision-register.md)
- [engineering_backlog.md](engineering_backlog.md)
