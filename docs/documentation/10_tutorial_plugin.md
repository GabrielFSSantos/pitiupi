# Tutorial — criar um plugin

Este guia complementa [09_sistema_de_plugins.md](09_sistema_de_plugins.md). Use `plugins/chat` como modelo.

## 1. Novo projeto

- Copie a pasta `plugins/chat` para `plugins/meuPlugin` (nome à escolha).
- Ajuste `project.properties` / `build.xml` com o nome do JAR de saída.
- Mantenha `javac.classpath` apontando para `../pitiupi/dist/pitiupi.jar`.

## 2. Classe principal

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
        if (message instanceof MinhaMessage m) { /* ... */ }
    }

    // getName, getAuthor, getVersion, getIdentifier
}
```

## 3. Mensagem

```java
public class MinhaMessage extends Message implements Serializable {
    // campos + serialVersionUID recomendado
}
```

Envio típico via `ConnectionManager` (multicast ou unicast UDP, ou TCP conforme o caso).

## 4. Service Provider Interface

Arquivo `src/META-INF/services/pitiupi.models.Plugin`:

```
pacote.MeuPlugin
```

## 5. Registrar no catálogo

Adicione uma entrada em [`plugins.registry.json`](../../plugins.registry.json):

```json
{
  "id": "xadrez",
  "dir": "pitiupi.xadrez",
  "build": "ant",
  "jar": "dist/pitiupi.xadrez.jar",
  "menuCategory": "Jogos"
}
```

O `id` é o que você passa para `./scripts/pitiupi.sh run --plugins xadrez`.

## 6. Build e instalação

```bash
./scripts/pitiupi.sh build --plugins xadrez
```

JARs de runtime ficam em `PITIUPI_HOME/plugins` (ver [00_quickstart.md](00_quickstart.md)).

## 7. Próximos passos

- [11_plugins_referencia.md](11_plugins_referencia.md) — chat e screenCast
- [17_tutorial_ping_aula.md](17_tutorial_ping_aula.md) — plugin didático Ping (opcional, fora do commit)
