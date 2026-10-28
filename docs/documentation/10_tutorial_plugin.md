# Tutorial — criar um plugin

Complementa [09_sistema_de_plugins.md](09_sistema_de_plugins.md). Use `plugins/chat` como **modelo de estrutura** (Ant, SPI, painel).

Os exemplos abaixo são **parciais** — você completa métodos, UI e testes.

## Verificações progressivas

| Etapa | O que fazer | Sucesso | Erro comum |
| --- | --- | --- | --- |
| Projeto Ant | Copiar `plugins/chat` → `plugins/meuPlugin`, ajustar JAR | `ant jar` OK | Classpath sem `pitiupi.jar` |
| Classe `Plugin` | Estender `Plugin`, metadados | Compila | `createPlugin` com assinatura antiga |
| SPI | `META-INF/services/pitiupi.models.Plugin` | Classe listada no JAR | Arquivo fora de `src/` |
| Mensagem | Uma classe `extends Message` | Em `getMessages()` | Tipo não registrado → ignorado na rede |
| Instalação | JAR em `PITIUPI_HOME/plugins` + reinício | Plugin na galeria | Esqueceu reiniciar |
| Rede | Duas instâncias, portas diferentes | Peer na lista | Portas iguais no mesmo PC ou firewall |

## 1. Novo projeto

- Copie `plugins/chat` para `plugins/meuPlugin`.
- Ajuste `project.properties` / `build.xml` com o nome do JAR (`dist/pitiupi.meuplugin.jar`).
- `javac.classpath` → `../../pitiupi/dist/pitiupi.jar` (ou caminho equivalente).

## 2. Classe principal (trecho)

```java
public class MeuPlugin extends Plugin {
    private PluginWorkspace workspace;

    @Override
    public void createPlugin(ConnectionManager cm, PluginWorkspace workspace) {
        this.workspace = workspace;
        MeuPanel panel = new MeuPanel(cm);
        bindShellWorkspaceContent(panel);
        installPeerActions(workspace.peerActions());
    }

    @Override
    public void installPeerActions(PeerActionsRegistrar registrar) {
        registrar.register(PeerAction.of(
                "meuplugin.abrir",
                "Abrir",
                PeerActionTarget.ALL_PEERS,
                target -> workspace.openOrFocusTab(getIdentifier(), getDisplayName())
        ));
    }

    @Override
    public List<Class<? extends Message>> getMessages() {
        return List.of(MinhaMessage.class);
    }

    @Override
    public void receiveMessage(Message message) {
        if (message instanceof MinhaMessage m) {
            // atualizar UI com SwingUtilities.invokeLater quando necessário
        }
    }
}
```

Implemente `getName()`, `getAuthor()`, `getVersion()`, `getIdentifier()` (identificador único na sala).

## 3. Mensagem e envio

```java
public class MinhaMessage extends Message implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String texto;
    // construtor, getters
}
```

Envio típico via `MessageDispatcher` (modo `TransportMode` + destino). Exemplo conceitual:

```java
// dentro do painel ou controller, com ConnectionManager injetado:
dispatcher.send(minhaMsg, TransportMode.UNICAST_UDP, SendTarget.forPeer(ip));
```

Detalhes: [07_mensagens_e_serializacao.md](07_mensagens_e_serializacao.md), [12_menus_unicast_multicast.md](12_menus_unicast_multicast.md).

## 4. SPI

`src/META-INF/services/pitiupi.models.Plugin`:

```
pacote.MeuPlugin
```

Confirme com `jar tf dist/seu.jar | grep META-INF/services`.

## 5. Catálogo (opcional)

Para build com `./scripts/pitiupi.sh`, adicione entrada em [`plugins.registry.json`](../../plugins.registry.json). Módulos só locais podem instalar o JAR pela aba **Configurações** sem entrar no catálogo.

## 6. Build e instalação

```bash
cd plugins/meuPlugin && ant jar
# copiar ou Instalar JAR… na UI; reiniciar o app
```

## 7. Dois cenários de teste

**A — Mesmo computador:** duas instâncias, portas `5000` e `5001`, nomes distintos, mesma versão do JAR.

**B — Dois PCs:** mesma porta (ex. `5000`), mesma sub-rede Wi‑Fi ou cabo, firewall UDP/TCP liberado, mesmo plugin instalado. Ver [appendix_portas_e_multicast.md](appendix_portas_e_multicast.md).

## Problemas comuns

| Sintoma | Causa provável | O que fazer |
| --- | --- | --- |
| Plugin não aparece | SPI ausente ou sem reinício | Verificar JAR, reiniciar |
| `ClassNotFoundException` na rede | Classe de mensagem não em `getMessages()` ou JAR diferente no peer | Alinhar tipos e reinstalar nos dois |
| Peers não se veem | Porta/sub-rede/multicast | Appendix Wi‑Fi |
| UI não atualiza | Rede na EDT | `SwingUtilities.invokeLater` |
| TCP falha | Peer offline ou IP errado | Conferir lista lateral e `PeerIpv4` |

## Próximos passos

- [11_plugins_referencia.md](11_plugins_referencia.md) — chat e screencast
