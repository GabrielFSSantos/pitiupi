# Mensagens

### Message

A classe `Message` representa a estrutura base das mensagens trocadas pela aplicação.

Todas as mensagens utilizadas pelo sistema devem herdar dessa classe. Ela implementa
`Serializable`, permitindo que objetos de mensagem sejam convertidos em vetores de 
bytes para transmissão pela rede.

A serialização é realizada pelo método `toByteArray()`, que transforma uma instância 
da mensagem em uma representação binária enviada pelos mecanismos de comunicação.

As mensagens específicas devem estender essa classe, adicionando os atributos e 
comportamentos necessários para cada funcionalidade.

A camada de transporte não depende do conteúdo específico das mensagens. Tanto o 
UDP quanto o TCP recebem mensagens serializadas como `byte[]`.

## Despacho aos plugins

1. **Registro:** cada plugin declara `List<Class<? extends Message>> getMessages()`.
2. **Recepção UDP:** `SocketUDP` entrega bytes ao `PluginManager`, que usa `PluginObjectInputStream` com o classloader do JAR do plugin.
3. **Recepção TCP:** `TcpConnection` lê um frame completo e o `PluginManager` tenta o mesmo despacho.
4. **Tipos desconhecidos:** se nenhum plugin reconhece, a mensagem é ignorada (sem crash do núcleo).

Mensagens de infraestrutura (`HeartbeatMessage`, `GoodbyeMessage`, etc.) são tratadas no núcleo antes ou em paralelo ao despacho de plugins — ver [08_heartbeat_e_presenca.md](08_heartbeat_e_presenca.md).

## Riscos da desserialização Java

Peers devem usar **mesma versão** das classes de mensagem. JARs de plugins devem vir de fonte confiável (instalação local pelo usuário). O núcleo **não** implementa sandbox de desserialização — ver [09_sistema_de_plugins.md](09_sistema_de_plugins.md).

Notas: [references/notas/java_serializacao.md](../references/notas/java_serializacao.md).
