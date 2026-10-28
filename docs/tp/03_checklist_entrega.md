# Checklist de entrega — trabalho prático

Complementa os testes do produto em [15_testes_e_gate.md](../documentation/15_testes_e_gate.md).

## Rede em sala (dois PCs, mesma Wi‑Fi)

Mesma **porta** em ambos (ex. 5000), **mesmos JARs** de referência em `~/pitiupi/plugins` em cada máquina:

- [ ] Presença e linha **Todos**
- [ ] Troca de mensagens no chat (multicast e, se possível, unicast/TCP)
- [ ] Compartilhamento de tela entre os dois (ambiente com captura de tela funcional — no WSL a captura nativa pode falhar)
- [ ] Plugin **Ping** ( [02_tutorial_ping_aula.md](02_tutorial_ping_aula.md) ) medindo RTT — recomendado antes da entrega final

## Plugin entregue (seu trabalho)

- [ ] JAR em `PITIUPI_HOME/plugins` + SPI correto
- [ ] README do módulo com build e formato das mensagens
- [ ] UI na aba (`PluginWorkspace`), sem `JFrame` principal
- [ ] Demonstração mínima professor ↔ aluno ou aluno ↔ aluno

## Ping (exercício de sala)

- [ ] Concluído conforme checklist em [02_tutorial_ping_aula.md](02_tutorial_ping_aula.md)
