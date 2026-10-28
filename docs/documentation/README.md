# Documentação técnica — Pitiupi

Índice geral: [docs/README.md](../README.md) · Convenções: [00_convencoes.md](00_convencoes.md).

## Por área

| Área | Arquivos |
| --- | --- |
| **Início** | [00_quickstart](00_quickstart.md), [02_modulos](02_modulos_do_repositorio.md), [03_build](03_build_ant_netbeans.md) |
| **Arquitetura e GUI** | [04_inicializacao_e_gui](04_inicializacao_e_gui.md), [13_guia_desenvolvimento](13_guia_desenvolvimento.md), [14_arquitetura_e_manutencao](14_arquitetura_e_manutencao.md) |
| **Rede** | [05_udp_multicast](05_udp_multicast.md), [06_tcp_conexoes](06_tcp_conexoes.md), [07_mensagens](07_mensagens_e_serializacao.md), [08_heartbeat](08_heartbeat_e_presenca.md), [16_fundamentacao_redes](16_fundamentacao_redes.md), [appendix_portas_e_multicast](appendix_portas_e_multicast.md) |
| **Plugins** | [09_sistema_de_plugins](09_sistema_de_plugins.md), [10_tutorial_plugin](10_tutorial_plugin.md), [11_plugins_referencia](11_plugins_referencia.md), [12_transporte_e_peer_actions](12_menus_unicast_multicast.md) |
| **Qualidade** | [15_testes_e_gate](15_testes_e_gate.md), [engineering_backlog](engineering_backlog.md) |

## Ordem linear (referência)

| # | Arquivo | Conteúdo |
| --- | --- | --- |
| 0 | [00_quickstart.md](00_quickstart.md) | Build, plugins, duas instâncias |
| 2–3 | 02–03 | Módulos, Ant |
| 4 | [04_inicializacao_e_gui.md](04_inicializacao_e_gui.md) | Boot, `MainShellFrame`, shell Swing |
| 5–8 | 05–08 | UDP, TCP, mensagens, presença |
| 9–12 | 09–12 | Plugins, tutorial genérico, transporte na UI |
| 13–15 | 13–15 | Dev, manutenção, testes |
| 16 | [16_fundamentacao_redes.md](16_fundamentacao_redes.md) | Fundamentação redes ↔ código |
| — | [appendix_portas_e_multicast.md](appendix_portas_e_multicast.md) | Portas, Wi‑Fi, WSL |
