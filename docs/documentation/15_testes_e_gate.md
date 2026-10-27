# Testes e gate

## Gate automatizado (núcleo)

O módulo `pitiupi/` inclui testes JUnit:

```bash
cd pitiupi && ant test
```

Falha se `java`, `javac` ou Ant estiverem ausentes.

Pacotes com testes automatizados (JUnit 5): `platform` (`PitiupiHome`, `ConfigLoader`), `plugins.store`, `application.presence`, `application.peeractions`, `application.invites`, `application.messaging`, `application.activity`, `presentation.shell`, `control` (goodbye), `net.tcp` (fechamento de conexões), `models`.

## Gate de build (produto)

Na raiz do clone:

```bash
./scripts/pitiupi.sh build --all
```

Equivalente manual:

```bash
cd pitiupi && ant jar
cd plugins/chat && ant jar
cd plugins/screencast && ant jar
```

Depois, para runtime local:

```bash
./scripts/pitiupi.sh link --all
```

## Checklist manual — uma máquina (5000 / 5001)

- [ ] Duas instâncias com portas distintas; nomes diferentes
- [ ] Peers visíveis na lista em até ~15 s
- [ ] Chat: sala geral e conversa por IP; tags de transporte coloridas no histórico
- [ ] ScreenCast: abrir aba, destino sala ou peer; fechar aba com × interrompe compartilhamento
- [ ] Compose do chat sem barra de rolagem vazio em janela ~1024×768
- [ ] Encerrar uma instância remove o peer após timeout de heartbeat

## Checklist manual — dois PCs (mesma Wi‑Fi)

Mesma **porta** em ambos (ex. 5000), **mesmos JARs** em `~/pitiupi/plugins` em cada máquina:

- [ ] Presença e linha **Todos**
- [ ] Troca de mensagens no chat (multicast e, se possível, unicast/TCP)
- [ ] Compartilhamento de tela entre os dois (ambiente com captura de tela funcional — no WSL a captura nativa pode falhar)
- [ ] Plugin Ping do aluno (tutorial [17_tutorial_ping_aula.md](17_tutorial_ping_aula.md)) medindo RTT — recomendado antes da entrega final

## Checklist plugin de aluno

- [ ] JAR em `PITIUPI_HOME/plugins` + SPI correto
- [ ] README do módulo com build e formato das mensagens
- [ ] UI na aba (`PluginWorkspace`), sem `JFrame` principal
- [ ] Demonstração mínima professor ↔ aluno ou aluno ↔ aluno

## Melhorias futuras

Ver [engineering_backlog.md](engineering_backlog.md) (CI contínuo, mais testes de rede sem sockets reais).
