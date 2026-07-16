# pitiupi

Ferramenta p2p para práticas em rede

Este repositório tem 3 projetos Java que são independentes:
- A ferramenta pitiupi (P2P)
- Um plugin de Chat
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
ln -s ../../pitiupi.screenCast/dist/*.jar .
``` 
Isso deve resolver!

Para criar um novo plugin:

