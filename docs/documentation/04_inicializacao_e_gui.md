# Visão geral

O `Pitiupi` é uma plataforma P2P baseada em uma arquitetura de plugins, 
desenvolvida para permitir a criação de aplicações distribuídas sobre uma 
infraestrutura comum de comunicação em rede.

A aplicação fornece mecanismos de descoberta de usuários, comunicação entre 
peers, troca de mensagens serializadas e integração de extensões por meio de 
plugins. A comunicação de rede utiliza dois protocolos de transporte: UDP, 
empregado principalmente na descoberta e comunicação multicast entre as 
instâncias, e TCP, utilizado para comunicação direta e persistente entre peers.

O projeto foi desenvolvido como suporte às práticas da disciplina de Redes de 
Computadores, servindo também como base para experimentação de aplicações 
distribuídas e desenvolvimento de projetos acadêmicos.

Índice: [README.md](README.md).

# Arquitetura geral

A aplicação é dividida em três grupos principais:

- **Núcleo da aplicação**: a `MainWindow` coordena GUI, rede e plugins.
- **Sistema de plugins**: extensões em JAR carregadas de `pitiupi/plugins/`.
- **Comunicação de rede**: UDP (`SocketUDP`) e TCP (`SocketTCP`, `TcpConnection`).

## Inicialização da aplicação

1. A `MainWindow` é criada após o diálogo de login (nome e porta).
2. O `SocketUDP` associa-se à porta configurada e ingressa no grupo multicast.
3. O `SocketTCP` cria um `ServerSocket` na mesma porta para conexões TCP.
4. Os mecanismos de comunicação iniciam threads de recepção.
5. O `HeartbeatManager` envia heartbeats periodicamente.
6. O painel de usuários online registra-se como listener do heartbeat.
7. O `PluginManager` carrega os JARs em `plugins/` via SPI.

Ver também: [05_udp_multicast.md](05_udp_multicast.md), [09_sistema_de_plugins.md](09_sistema_de_plugins.md).

# Interface gráfica

A interface gráfica da aplicação é baseada em Swing e é centralizada na
classe `MainWindow`.

Durante a inicialização, cada plugin recebe `ConnectionManager` e `GuiManager`
no método `createPlugin()`.

A extensão usual é registrar menus por categoria (`MenuCategory`). Ver
[12_menus_unicast_multicast.md](12_menus_unicast_multicast.md).

## Diagrama de comunicação

```
                         +------------------+
                         |    MainWindow    |
                         +--------+---------+
                                  |
             +--------------------+--------------------+
             |                    |                    |
             v                    v                    v
      HeartbeatManager       SocketUDP            SocketTCP
             |                    |                    |
             v                    v                    v
         Heartbeat           UDP Multicast       TCP Connections
                                  |                    |
                                  +---------+----------+
                                            |
                                            v
                                         Plugins
                                            |
                                            v
                                        Message
```

O UDP multicast sustenta presença e mensagens broadcast/unicast; o TCP sustenta
canais diretos com framing de mensagens. Detalhes em [05](05_udp_multicast.md)
e [06](06_tcp_conexoes.md).
