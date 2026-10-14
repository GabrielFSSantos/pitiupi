# Serialização Java (nota)

O Pitiupi serializa objetos `Message` (Java `Serializable`) para `byte[]` antes do envio.

Implicações:

- Mesma versão de classe nos peers (mesmo JAR de plugin).
- `serialVersionUID` explícito evita incompatibilidade silenciosa.
- Desserialização no núcleo usa `PluginObjectInputStream` com classloader do plugin.

Doc: [07_mensagens_e_serializacao.md](../../documentation/07_mensagens_e_serializacao.md).
