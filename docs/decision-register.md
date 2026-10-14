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

Novas decisões: adicionar linha nesta tabela e detalhar no arquivo técnico correspondente.
