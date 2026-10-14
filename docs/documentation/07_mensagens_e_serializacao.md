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
