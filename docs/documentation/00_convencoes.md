# Convenções da documentação

Índice geral: [docs/README.md](../README.md).

## Idioma e tom

- Português brasileiro, termos de Redes e Java como no código (`ConnectionManager`, `MulticastSocket`).
- Parágrafos completos; evitar listas longas quando um texto curto explica melhor.

## Blocos teoria × implementação

Em capítulos de rede e arquitetura, quando um conceito for central, use mentalmente três camadas:

1. **Teoria** — como o protocolo ou padrão funciona em geral (com link para [references/README.md](../references/README.md) se precisar de fonte).
2. **No Pitiupi** — classes e fluxo reais.
3. **Limitação** — o que o projeto **não** faz (ex.: sem criptografia, sem ICMP nativo no plugin Ping).

Não descreva funcionalidade futura como se já existisse.

## Código nos exemplos

- Trechos de tutorial são **ilustrativos** quando indicado; complete o plugin conforme [10_tutorial_plugin.md](10_tutorial_plugin.md).
- APIs citadas devem existir no núcleo atual (`PluginWorkspace`, `createPlugin(ConnectionManager, PluginWorkspace)`).

## Diagramas

- Preferir [Mermaid](https://github.blog/2014-02-14-render-diagrams-markdown/) em arquivos `.md` (GitHub).
- Diagramas pequenos, um fluxo por figura.

## Manutenção

Alterou protocolo, SPI ou boot? Atualize o capítulo numerado correspondente e, se for decisão estável, [decision-register.md](../decision-register.md). Ver [13_guia_desenvolvimento.md](13_guia_desenvolvimento.md).
