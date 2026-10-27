# Arquitetura e manutenção

## Onde editar

| Comportamento | Pacote / classe |
| --- | --- |
| Login, porta, nome | `pitiupi.presentation.shell.LoginDialog` |
| Shell principal (abas, galeria) | `pitiupi.presentation.shell.MainShellFrame`, `TabWorkspacePanel` |
| Menu Ajuda | `pitiupi.presentation.shell.ShellMenuBar` |
| Usuários online, peer actions | `pitiupi.presentation.shell.NetworkUsersPanel`, `PeerActionBroker` |
| Facade rede para plugins | `pitiupi.control.ConnectionManager` |
| Workspace na aba (SPI) | `pitiupi.api.plugin.PluginWorkspace`, `ShellPluginWorkspace` |
| UDP | `pitiupi.net.udp.SocketUDP` |
| TCP servidor e mapa de conexões | `pitiupi.net.tcp.SocketTCP`, `TcpConnectionManager` |
| Conexão TCP por peer | `pitiupi.net.tcp.TcpConnection` |
| Handshake / fábrica TCP | `pitiupi.net.tcp.ConnectionFactory`, `HandshakeMessage` |
| Heartbeat | `pitiupi.control.HeartbeatManager`, `HeartbeatMessage` |
| Carregar JARs, despacho | `pitiupi.control.plugins.PluginManager` |
| Contrato plugin | `pitiupi.models.Plugin`, `pitiupi.models.Messages.Message` |
## Dependências entre módulos

```
pitiupi (núcleo)
   ↑ classpath
plugins/chat | plugins/screencast
```

Plugins **não** devem ser dependência do núcleo.

## Alterações arriscadas

- Mudar grupo multicast ou formato de heartbeat → quebra interoperabilidade na sala.
- Mudar framing TCP → todos os plugins que usam TCP precisam acompanhar.
- Alterar nome do arquivo SPI (`pitiupi.models.Plugin`) → todos os JARs de plugin.

Registrar decisão em [decision-register.md](../decision-register.md).

## Documentação viva

Ao mudar protocolo ou API pública do núcleo, atualizar o número correspondente em [documentation/README.md](README.md) e, se aplicável, [11_plugins_referencia.md](11_plugins_referencia.md).
