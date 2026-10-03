# pitiupi

Este repositório tem 4 projetos Java que são independentes:
######## Sistema:
- A ferramenta pitiupi (P2P)
######## Plugins: 
- Um plugin de Chat
- Um plugin de Chat multiprotocolo (TCP/UDP)
- Um plugin de ScreenCast

Cada um destes projetos deve ser compilado individualmente. Para isso, use a ferramenta ant no diretório do projeto.
Ex:
```
cd pitiupi.chat
ant jar
``` 

Feito isso, os arquivos jar dos plugins deverão ser copiados ou linkados na pasta plugin da ferramenta.
No caso de a mesma ser linkada, ao recompilar os plugins não é necessário copiar o mesmo novamente para a pasta de plugins da ferramenta.
Para criar links simbólicos no Linux, use o comando:

```
cd pitiupi/plugins
ln -s ../../pitiupi.chat/dist/*.jar .
ln -s ../../pitiupi.mpChat/dist/*.jar .
ln -s ../../pitiupi.screenCast/dist/*.jar .
``` 
Isso deve resolver!

## Como um plugin conversa com o pitiupi

Um plugin nunca recebe acesso direto às classes internas do núcleo (ConnectionManager, GuiManager). Em vez disso, recebe uma facade, a classe Pitiupi, que expõe só o que um plugin precisa: enviar mensagem (multicast, unicast ou tcp), abrir conexão tcp privada, ler nome de usuário e porta, criar menu.

```java
public abstract void createPlugin(Pitiupi pitiupi);
```

## Para criar um novo plugin

1. Implemente `pitiupi.models.Plugin`, incluindo `createPlugin(Pitiupi pitiupi)`
2. Registre a classe em `src/META-INF/services/pitiupi.models.Plugin`, com o nome completo da classe numa linha
3. Compile com `ant jar`
4. Linke o `.jar` em `pitiupi/plugins`

Use `pitiupi.chat` como referência — é o exemplo mais simples e completo.

Plugins que ainda não foram atualizados para a Pitiupi (como uma versão mais antiga do screenCast) não compilam contra o núcleo atual e precisam de migração antes de serem usados.
