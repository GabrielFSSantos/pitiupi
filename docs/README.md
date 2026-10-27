# Documentação — Pitiupi

Entrada na raiz do repositório: [README.md](../README.md).

## Pilares

| Pasta / arquivo | Papel |
| --- | --- |
| **[documentation/](documentation/README.md)** | Código, rede, plugins, operação e guia para alunos |
| **[references/](references/README.md)** | Bibliografia e notas de conceitos (UDP, TCP, SPI) |
| **[decision-register.md](decision-register.md)** | Decisões de desenho da plataforma |

## Público-alvo

| Leitor | Começar por |
| --- | --- |
| **Aluno (TP)** | [documentation/00_quickstart.md](documentation/00_quickstart.md) → [10_tutorial_plugin.md](documentation/10_tutorial_plugin.md) → [17_tutorial_ping_aula.md](documentation/17_tutorial_ping_aula.md) → [11_plugins_referencia.md](documentation/11_plugins_referencia.md) |
| **Monitor / docência** | [documentation/01_visao_disciplina_e_tp.md](documentation/01_visao_disciplina_e_tp.md) → [15_testes_e_gate.md](documentation/15_testes_e_gate.md) |
| **Desenvolvedor** | [documentation/13_guia_desenvolvimento.md](documentation/13_guia_desenvolvimento.md) → [14_arquitetura_e_manutencao.md](documentation/14_arquitetura_e_manutencao.md) |
| **Professor / banca** | [documentation/16_fundamentacao_redes.md](documentation/16_fundamentacao_redes.md) → [references/README.md](references/README.md) |

**Idioma:** documentação em PT-BR; pacotes e classes Java como no código.

## Manutenção

| Tipo de mudança | Onde documentar |
| --- | --- |
| Protocolo UDP/TCP, multicast, portas | [documentation/05_udp_multicast.md](documentation/05_udp_multicast.md), [06_tcp_conexoes.md](documentation/06_tcp_conexoes.md), [appendix_portas_e_multicast.md](documentation/appendix_portas_e_multicast.md) |
| Contrato de plugin / SPI | [documentation/09_sistema_de_plugins.md](documentation/09_sistema_de_plugins.md), [10_tutorial_plugin.md](documentation/10_tutorial_plugin.md) |
| Novo plugin de referência | [documentation/11_plugins_referencia.md](documentation/11_plugins_referencia.md) |
| Decisão de arquitetura | [decision-register.md](decision-register.md) |
| Débito técnico | [documentation/engineering_backlog.md](documentation/engineering_backlog.md) |
| Literatura de redes | [references/](references/) |

Documentação legada em `pitiupi/docs/` redireciona para esta árvore.
