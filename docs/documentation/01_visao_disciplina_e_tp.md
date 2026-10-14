# Visão da disciplina e trabalho prático

## Papel do Pitiupi

O Pitiupi é uma **plataforma P2P com plugins** usada em Redes de Computadores para:

- Experimentar UDP (multicast/unicast) e TCP na mesma aplicação.
- Observar **presença** de peers (heartbeat) sem servidor central de aplicação.
- Desenvolver **trabalhos individuais ou em grupo** como novos plugins (jogos, ferramentas colaborativas, etc.).

O núcleo fornece rede e GUI; o aluno implementa a lógica no plugin.

## Trabalho típico (plugin)

Cada equipe/aluno entrega um **módulo Java separado** que:

1. Compila para JAR e carrega via `pitiupi/plugins/`.
2. Define mensagens (`extends Message`) e o fluxo entre peers.
3. Integra menus/janelas via `GuiManager`.
4. Inclui README do módulo (build + protocolo).

Exemplos de tema: chat temático, tabuleiro (xadrez/damas), compartilhamento de arquivo, quiz síncrono.

## Plugins de referência no repositório

| Módulo | Ideia pedagógica |
| --- | --- |
| `pitiupi.chat` | Mensagens UDP + UI simples |
| `pitiupi.screenCast` | Payload maior / streaming de imagem |
| `pitiupi.mpChat` | Multicast + TCP privado no mesmo plugin |

Detalhes: [11_plugins_referencia.md](11_plugins_referencia.md).

## Critérios genéricos (ajustar na disciplina)

| Critério | Indicador |
| --- | --- |
| Funcionalidade | Comportamento correto entre dois+ peers na rede da sala |
| Uso da plataforma | SPI, mensagens, sem “fork” desnecessário do núcleo |
| Build | `ant jar` / `make` reproduzível |
| Documentação | README do plugin + descrição das mensagens |

## Para monitores

- Roteiro de build: [00_quickstart.md](00_quickstart.md).
- Checklist de entrega: [15_testes_e_gate.md](15_testes_e_gate.md).
- Fundamentação para dúvidas de alunos: [16_fundamentacao_redes.md](16_fundamentacao_redes.md).
