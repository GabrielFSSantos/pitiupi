# Arquitetura e manutenção

## Onde editar

| Comportamento | Pacote / classe |
| --- | --- |
| Login, porta, nome | `pitiupi.GUI.Login` |
| Orquestração GUI + plugins | `pitiupi.GUI.MainWindow` |
| Barra de menus base | `pitiupi.GUI.MenuBar` |
| Usuários online | `pitiupi.GUI.OnlineUsersPanel`, `HeartbeatManager` |
| Facade rede para plugins | `pitiupi.control.ConnectionManager` |
| Registro de menus | `pitiupi.control.GuiManager` |
| UDP | `pitiupi.net.udp.SocketUDP` |
| TCP servidor e mapa de conexões | `pitiupi.net.tcp.SocketTCP`, `TcpConnectionManager` |
| Conexão TCP por peer | `pitiupi.net.tcp.TcpConnection` |
| Handshake / fábrica TCP | `pitiupi.net.tcp.ConnectionFactory`, `HandshakeMessage` |
| Heartbeat | `pitiupi.control.HeartbeatManager`, `HeartbeatMessage` |
| Carregar JARs, despacho | `pitiupi.control.plugins.PluginManager` |
| Contrato plugin | `pitiupi.models.Plugin`, `pitiupi.models.Messages.Message` |
| Menus por categoria | `pitiupi.models.Menus.*` |

## Dependências entre módulos

```
pitiupi (núcleo)
   ↑ classpath
pitiupi.chat | pitiupi.screenCast | pitiupi.mpChat
```

Plugins **não** devem ser dependência do núcleo.

## Alterações arriscadas

- Mudar grupo multicast ou formato de heartbeat → quebra interoperabilidade na sala.
- Mudar framing TCP → todos os plugins que usam TCP precisam acompanhar.
- Alterar nome do arquivo SPI (`pitiupi.models.Plugin`) → todos os JARs de plugin.

Registrar decisão em [decision-register.md](../decision-register.md).

## Documentação viva

Ao mudar protocolo ou API pública do núcleo, atualizar o número correspondente em [documentation/README.md](README.md) e, se aplicável, [11_plugins_referencia.md](11_plugins_referencia.md).
