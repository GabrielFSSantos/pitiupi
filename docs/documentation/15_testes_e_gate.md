# Testes e gate

## Estado atual

Não há suíte JUnit integrada ao repositório. A validação principal é:

1. **Build** de todos os módulos.
2. **Teste manual** com duas instâncias na rede.

## Gate de build (recomendado)

Sequência:

```bash
cd pitiupi && ant jar
cd ../pitiupi.chat && ant jar
cd ../pitiupi.screenCast && ant jar
cd ../pitiupi.mpChat && make
```

Falha se `java`, **`javac`**, Ant ou Make estiverem ausentes (instale `openjdk-*-jdk`, não só JRE).

## Checklist manual (rede)

- [ ] Duas instâncias com portas distintas (mesmo host) ou dois hosts na mesma LAN
- [ ] Nomes visíveis no painel de usuários em até ~15 s
- [ ] Plugin de referência troca mensagens entre os dois
- [ ] Encerrar uma instância remove o peer da lista após timeout de heartbeat

## Checklist plugin de aluno

- [ ] JAR em `plugins/` + SPI correto
- [ ] README do módulo com build e formato das mensagens
- [ ] Demonstração mínima professor ↔ aluno ou aluno ↔ aluno

## Melhorias futuras

Ver [engineering_backlog.md](engineering_backlog.md) (testes automatizados, CI).
