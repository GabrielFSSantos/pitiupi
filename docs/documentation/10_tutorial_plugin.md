# Tutorial — criar um plugin

Este guia complementa [09_sistema_de_plugins.md](09_sistema_de_plugins.md). Use `pitiupi.chat` como modelo.

## 1. Novo projeto

- Copie a pasta `pitiupi.chat` para `pitiupi.meuPlugin` (nome à escolha).
- Ajuste `project.properties` / `build.xml` com o nome do JAR de saída.
- Mantenha `javac.classpath` apontando para `../pitiupi/dist/pitiupi.jar`.

## 2. Classe principal

```java
public class MeuPlugin extends Plugin {
    @Override
    public void createPlugin(ConnectionManager cm, GuiManager gui) {
        // registrar menus, abrir janelas
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

## 5. Build e instalação

```bash
cd pitiupi.meuPlugin && ant jar
ln -sf ../../pitiupi.meuPlugin/dist/*.jar ../pitiupi/plugins/
```

Reinicie o Pitiupi após trocar JARs.

## 6. Teste

Duas instâncias, mesma rede, portas distintas se no mesmo host. Ver [00_quickstart.md](00_quickstart.md).

## TCP dedicado (avançado)

Se o plugin precisa de canal TCP com handshake por `getIdentifier()`, estude `pitiupi.mpChat` e os métodos opcionais em `Plugin` (`receivePrivateConnection`, `receiveBytes`). Ver [11_plugins_referencia.md](11_plugins_referencia.md).

## Erros frequentes

| Problema | Causa provável |
| --- | --- |
| Plugin não aparece | SPI ausente ou JAR fora de `plugins/` |
| `ClassNotFoundException` na recepção | Classe da mensagem não no JAR do remetente/destino |
| Peers não se veem | Multicast bloqueado — [appendix_portas_e_multicast.md](appendix_portas_e_multicast.md) |
