# Registro de decisões — Pitiupi

Decisões que o código atual segue. O detalhe está nos capítulos ligados.

| ID | Decisão | Motivação | Onde ler |
| --- | --- | --- | --- |
| D1 | Grupo multicast `224.0.0.3` | Endereço fixo para descoberta e broadcast na LAN da sala | [rede.md](rede.md) |
| D2 | Mesma porta UDP e TCP por instância | Configurada no login; simplifica o firewall em laboratório | [comecar.md](comecar.md) |
| D3 | Heartbeat via UDP multicast | Presença sem conexão TCP permanente com todos os peers | [rede.md](rede.md) |
| D4 | Timeout de peer cerca de 15 s sem heartbeat | Remover usuários inativos da lista | [rede.md](rede.md) |
| D5 | Framing TCP: `int` tamanho + bytes | Delimitar mensagens num fluxo de bytes | [rede.md](rede.md) |
| D6 | Mensagens `Serializable` em `byte[]` | O transporte não depende dos campos; o plugin define o tipo | [rede.md](rede.md) |
| D7 | Plugins via JAR + SPI | Carregar uma classe nova sem recompilar o núcleo | [plugins.md](plugins.md) |
| D8 | Classloader por JAR | Isolar classes e desserializar com `PluginObjectInputStream` | [plugins.md](plugins.md) |
| D9 | Plugins em `PITIUPI_HOME/plugins` | Dados de execução em `.pitiupi/` na raiz do clone. `PITIUPI_HOME` pode apontar outro diretório | [comecar.md](comecar.md) |
| D10 | Erros com `ErrorCode` | Mensagens em português na janela; `PitiupiException` no domínio | [nucleo.md](nucleo.md) |
| D11 | Reinício após instalar ou remover JAR | A lista de plugins é lida na subida | [usar.md](usar.md) |
| D16 | UI em abas, sem `JFrame` de plugin | O painel fica na `MainShellFrame`; galeria e peer actions abrem a aba | [nucleo-para-plugins.md](nucleo-para-plugins.md) |
| D17 | Goodbye e timeout de presença | `GoodbyeMessage` em multicast ao fechar; timeout de cerca de 15 s | [rede.md](rede.md) |
| D18 | Peer actions e convites | `PeerActionBroker` e `SessionInviteMessage` na shell | [plugins.md](plugins.md) |
| D19 | Chat num único módulo | `plugins/chat` cobre sala, um IP e TCP privado | [plugins.md](plugins.md) |
| D20 | Peer actions de todos os plugins no popup | A lista agrupa ações em submenu por plugin, não só da aba aberta | [nucleo-para-plugins.md](nucleo-para-plugins.md) |
| D21 | `onShellTabClosed` | Fechar a aba avisa o plugin para parar timer ou captura | [nucleo-para-plugins.md](nucleo-para-plugins.md) |
| D22 | `PeerIpv4` na interface | No Windows o endereço pode vir mapeado em IPv6 ou com sufixo de zona | [rede.md](rede.md) |
| D23 | Sem selo numérico na lista | O recebimento aparece na aba do plugin. O núcleo guarda foco de atividade, a lista não desenha badge | [plugins.md](plugins.md) |
| D24 | `SessionNoticeMessage` | Início e fim de uma atividade (por exemplo o fim do compartilhamento de tela) | [plugins.md](plugins.md) |
| D25 | Nome novo no heartbeat já existente | A lista do colega atualiza o `User` quando o `userName` muda | [rede.md](rede.md) |
| D26 | Troca de porta sai da sala | Goodbye na porta antiga, lista local zerada, anúncio na porta nova | [rede.md](rede.md) |
| D27 | TTL padrão 1, de 1 a 32 | 1 permanece na sub-rede local | [rede.md](rede.md) |
| D28 | Saída Automático, Cabo ou Wi-Fi | Multicast, IP exibido e TCP de saída usam a interface escolhida | [rede.md](rede.md) |
| D29 | TCP com prazo de 5 s fora da thread da janela | O aviso aparece só enquanto não há socket aberto para aquele IP | [rede.md](rede.md) |
| D30 | `eth*` continua cabo, inclusive no WSL | A placa Wi-Fi do Windows não existe no Linux do WSL. Tratar todo `eth` como Wi-Fi quebraria o Ubuntu nativo | [rede.md](rede.md) |
