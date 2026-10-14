# Build — Ant, NetBeans e Make

## Pré-requisitos e versões Java

| Módulo | Build | `javac.source` / `target` (NetBeans) |
| --- | --- | --- |
| `pitiupi` | Ant | 17 |
| `pitiupi.chat` | Ant | 25 |
| `pitiupi.screenCast` | Ant | 25 |
| `pitiupi.mpChat` | Make | (sem `project.properties`; compila contra `pitiupi.jar`) |

Use um JDK que forneça `javac` (ex.: OpenJDK 25). Ant 1.10+ e Make no PATH.

## Núcleo e plugins NetBeans

Projetos com `nbproject/` e `build.xml`:

```bash
cd pitiupi && ant jar
cd pitiupi.chat && ant jar
cd pitiupi.screenCast && ant jar
```

Abrir no NetBeans: **File → Open Project** em cada pasta.

O target `jar` produz o executável do núcleo e os JARs dos plugins. O classpath de compilação dos plugins aponta para `../pitiupi/dist/pitiupi.jar` (ver `project.properties` de cada módulo).

## pitiupi.mpChat (Makefile)

```bash
cd pitiupi.mpChat
make
```

O Makefile compila com `javac -cp ../pitiupi/dist/pitiupi.jar`, copia `META-INF` e gera `dist/mpChat.jar`. **Compile o núcleo antes.**

## Diretório `plugins/`

A pasta `pitiupi/plugins/` contém os JARs carregados em runtime. Apenas arquivos `.jar` são lidos. Ver [pitiupi/plugins/README.md](../../pitiupi/plugins/README.md).

## Ordem recomendada de build

1. `pitiupi` (núcleo)
2. Plugins em qualquer ordem
3. Symlink ou cópia para `pitiupi/plugins/`

## Gate automatizado (docentes)

Quem mantém o meta-workspace Cursor pode usar o script de gate que executa `ant jar` nos três módulos Ant e `make` no mpChat (requer Ant/Make no PATH). Detalhes em [15_testes_e_gate.md](15_testes_e_gate.md).
