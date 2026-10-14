# Java SPI — plugins (nota)

O núcleo usa `ServiceLoader` para localizar implementações de `pitiupi.models.Plugin` declaradas em:

`META-INF/services/pitiupi.models.Plugin`

Uma linha por classe concreta (FQCN). O JAR deve estar em `pitiupi/plugins/`.

Doc: [09_sistema_de_plugins.md](../../documentation/09_sistema_de_plugins.md).
