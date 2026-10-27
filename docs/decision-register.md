# Registro de decisões — Pitiupi

| ID | Decisão | Motivação | Doc |
| --- | --- | --- | --- |
| D1 | Grupo multicast `224.0.0.3` | Endereço fixo para descoberta e broadcast na LAN da sala | [appendix_portas_e_multicast.md](documentation/appendix_portas_e_multicast.md) |
| D2 | Mesma porta UDP e TCP por instância | Configurada no login; simplifica firewall em laboratório | [00_quickstart.md](documentation/00_quickstart.md) |
| D3 | Heartbeat via UDP multicast | Presença sem conexão TCP permanente com todos os peers | [08_heartbeat_e_presenca.md](documentation/08_heartbeat_e_presenca.md) |
| D4 | Timeout de peer ~15 s sem heartbeat | Remover usuários inativos da lista | [08_heartbeat_e_presenca.md](documentation/08_heartbeat_e_presenca.md) |
| D5 | Framing TCP: `int` tamanho + bytes | Delimitar mensagens em stream orientado a bytes | [06_tcp_conexoes.md](documentation/06_tcp_conexoes.md) |
| D6 | Mensagens `Serializable` em `byte[]` | Independência do transporte; plugins definem tipos | [07_mensagens_e_serializacao.md](documentation/07_mensagens_e_serializacao.md) |
| D7 | Plugins via JAR + Java SPI | Carregamento dinâmico sem recompilar o núcleo | [09_sistema_de_plugins.md](documentation/09_sistema_de_plugins.md) |
| D8 | Classloader por JAR de plugin | Isolar dependências; desserialização com `PluginObjectInputStream` | [09_sistema_de_plugins.md](documentation/09_sistema_de_plugins.md) |
| D9 | Plugins em `PITIUPI_HOME/plugins` | Dados do usuário fora do clone Git; `PITIUPI_HOME` ou `~/pitiupi` | [00_quickstart.md](documentation/00_quickstart.md) |
| D10 | Erros com `ErrorCode` | Mensagens PT na UI; `PitiupiException` no domínio | [13_guia_dev_e_agente.md](documentation/13_guia_dev_e_agente.md) |
| D11 | Reinício após instalar/remover JAR | Mutar `plugins/` exige reiniciar o app para recarregar SPI | [04_inicializacao_e_gui.md](documentation/04_inicializacao_e_gui.md) |
| D16 | UI em abas; sem `JFrame` de plugin | Conteúdo na `MainShellFrame`; galeria e peer actions abrem abas | [04_inicializacao_e_gui.md](documentation/04_inicializacao_e_gui.md) |
| D17 | Goodbye + timeout de presença | `GoodbyeMessage` em multicast ao fechar; timeout ~15 s mantido | [08_heartbeat_e_presenca.md](documentation/08_heartbeat_e_presenca.md) |
| D18 | Peer actions + convites | `PeerActionBroker`, `SessionInviteMessage`, badge (UI completa na shell) | [09_sistema_de_plugins.md](documentation/09_sistema_de_plugins.md) |
| D19 | Chat unificado | Um JAR `plugins/chat` com sala geral, peer/IP e TCP privado; mpChat removido do catálogo | [11_plugins_referencia.md](documentation/11_plugins_referencia.md) |
| D20 | Peer actions por plugin no popup | Lista lateral agrupa ações de todos os plugins (submenus), não só a aba ativa | [09_sistema_de_plugins.md](documentation/09_sistema_de_plugins.md) |
| D21 | `onShellTabClosed` | Fechar aba do plugin na shell notifica o plugin (ex.: parar screencast) | [09_sistema_de_plugins.md](documentation/09_sistema_de_plugins.md) |
| D22 | `PeerIpv4` para IP de peer na UI | No Windows, `InetAddress.getHostAddress()` pode devolver IPv6-mapeado e sufixo de zona (`%n`); chat e roteamento usam IPv4 pontilhado | [appendix_portas_e_multicast.md](documentation/appendix_portas_e_multicast.md) |
| D23 | Foco de atividade e badges | **Adiado na v1** — núcleo (`PeerActivityFocus` / `PeerActivityInbox`) mantido; UI sem badges; recebimento passivo em sub-abas (`addTabIfAbsent`) | [09_sistema_de_plugins.md](documentation/09_sistema_de_plugins.md) |

Novas decisões: adicionar linha nesta tabela e detalhar no arquivo técnico correspondente.
